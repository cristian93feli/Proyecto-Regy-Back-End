package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.CompletarSolicitudRequestDTO;
import com.regyinventory.dto.request.CrearSolicitudRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.SolicitudResponseDTO;
import com.regyinventory.entities.LoteInventario;
import com.regyinventory.entities.MovimientoInventario;
import com.regyinventory.entities.Producto;
import com.regyinventory.entities.SolicitudReposicion;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.enums.EstadoSolicitud;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.TipoMovimiento;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IMovimientoInventarioRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.ISolicitudReposicionRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.service.contracts.ISolicitudReposicionService;
import com.regyinventory.utils.constants.api.ValoresApi;
import com.regyinventory.utils.constants.log.ConstantesLog;
import com.regyinventory.utils.constants.mensajes.MensajesError;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudReposicionService implements ISolicitudReposicionService {
    private final ISolicitudReposicionRepository solicitudReposicionRepository;
    private final IProductoRepository productoRepository;
    private final IUbicacionRepository ubicacionRepository;
    private final ILoteInventarioRepository loteInventarioRepository;
    private final IMovimientoInventarioRepository movimientoInventarioRepository;
    private final InventarioService inventarioService;
    private final OperacionSupport operacionSupport;

    @Override
    @Transactional
    public SolicitudResponseDTO crear(CrearSolicitudRequestDTO solicitudCreacion) {
        Producto producto = productoRepository.findById(solicitudCreacion.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Inventario.PRODUCTO_NO_ENCONTRADO));
        Ubicacion zonaDestino = obtenerZonaEmpaqueActiva(solicitudCreacion.getZonaDestinoId());
        SolicitudReposicion nuevaSolicitud = SolicitudReposicion.builder()
                .producto(producto)
                .cantidad(solicitudCreacion.getCantidad())
                .prioridad(solicitudCreacion.getPrioridad())
                .zonaDestino(zonaDestino)
                .usuarioSolicitante(operacionSupport.obtenerUsuarioAutenticado())
                .observaciones(solicitudCreacion.getObservaciones())
                .build();
        SolicitudReposicion solicitudGuardada = solicitudReposicionRepository.save(nuevaSolicitud);
        operacionSupport.registrarLog(
                TipoAccionLog.CREAR_SOLICITUD,
                ConstantesLog.Entidad.SOLICITUD_REPOSICION,
                solicitudGuardada.getId(),
                ConstantesLog.Detalle.SOLICITUD_CREADA
        );
        return convertirRespuesta(solicitudGuardada);
    }

    @Override
    public PageResponseDTO<SolicitudResponseDTO> listar(EstadoSolicitud estado, Integer numeroPagina, Integer tamanoPagina) {
        Pageable paginacion = PageRequest.of(
                numeroPagina,
                tamanoPagina,
                Sort.by(Sort.Direction.DESC, ValoresApi.CAMPO_FECHA_CREACION)
        );
        return PageResponseDTO.fromPage(
                estado == null
                        ? solicitudReposicionRepository.findAll(paginacion)
                        : solicitudReposicionRepository.findByEstado(estado, paginacion),
                this::convertirRespuesta
        );
    }

    @Override
    @Transactional
    public SolicitudResponseDTO completar(Long solicitudId, CompletarSolicitudRequestDTO solicitudCompletar) {
        SolicitudReposicion solicitudReposicion = obtenerSolicitud(solicitudId);
        if (solicitudReposicion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BusinessException(MensajesError.SolicitudReposicion.NO_PENDIENTE);
        }
        List<LoteInventario> lotesEnCajas = loteInventarioRepository.findAllByCantidadGreaterThan(0).stream()
                .filter(loteInventario -> loteInventario.getProducto().getId().equals(solicitudReposicion.getProducto().getId()))
                .filter(loteInventario -> loteInventario.getUbicacion().getTipo() == TipoUbicacion.CAJA)
                .sorted(Comparator.comparing(
                        (LoteInventario loteInventario) -> loteInventario.getFechaVencimiento() == null
                                ? LocalDate.MAX
                                : loteInventario.getFechaVencimiento()
                ).thenComparing(LoteInventario::getFechaIngreso))
                .toList();
        int cantidadDisponible = lotesEnCajas.stream().mapToInt(LoteInventario::getCantidad).sum();
        if (cantidadDisponible < solicitudReposicion.getCantidad()) {
            throw new BusinessException(MensajesError.SolicitudReposicion.STOCK_INSUFICIENTE_DEPOSITOS);
        }

        int cantidadPendiente = solicitudReposicion.getCantidad();
        for (LoteInventario loteOrigen : lotesEnCajas) {
            if (cantidadPendiente == 0) {
                break;
            }
            int cantidadTransferida = Math.min(cantidadPendiente, loteOrigen.getCantidad());
            inventarioService.transferirLotes(
                    solicitudReposicion.getProducto(),
                    cantidadTransferida,
                    loteOrigen.getUbicacion(),
                    solicitudReposicion.getZonaDestino()
            );
            MovimientoInventario movimientoReposicion = MovimientoInventario.builder()
                    .producto(solicitudReposicion.getProducto())
                    .cantidad(cantidadTransferida)
                    .tipoMovimiento(TipoMovimiento.REPOSICION)
                    .ubicacionOrigen(loteOrigen.getUbicacion())
                    .ubicacionDestino(solicitudReposicion.getZonaDestino())
                    .observaciones(solicitudCompletar == null ? null : solicitudCompletar.getObservaciones())
                    .usuario(operacionSupport.obtenerUsuarioAutenticado())
                    .build();
            movimientoInventarioRepository.save(movimientoReposicion);
            cantidadPendiente -= cantidadTransferida;
        }
        solicitudReposicion.setEstado(EstadoSolicitud.COMPLETADA);
        solicitudReposicion.setUsuarioResponsable(operacionSupport.obtenerUsuarioAutenticado());
        solicitudReposicionRepository.save(solicitudReposicion);
        operacionSupport.registrarLog(TipoAccionLog.COMPLETAR_SOLICITUD, ConstantesLog.Entidad.SOLICITUD_REPOSICION, solicitudId, ConstantesLog.Detalle.SOLICITUD_COMPLETADA);
        return convertirRespuesta(solicitudReposicion);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO cancelar(Long solicitudId) {
        SolicitudReposicion solicitudReposicion = obtenerSolicitud(solicitudId);
        if (solicitudReposicion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BusinessException(MensajesError.SolicitudReposicion.NO_PENDIENTE);
        }
        solicitudReposicion.setEstado(EstadoSolicitud.CANCELADA);
        solicitudReposicion.setUsuarioResponsable(operacionSupport.obtenerUsuarioAutenticado());
        solicitudReposicionRepository.save(solicitudReposicion);
        operacionSupport.registrarLog(TipoAccionLog.CANCELAR_SOLICITUD, ConstantesLog.Entidad.SOLICITUD_REPOSICION, solicitudId, ConstantesLog.Detalle.SOLICITUD_CANCELADA);
        return convertirRespuesta(solicitudReposicion);
    }

    private SolicitudReposicion obtenerSolicitud(Long solicitudId) {
        return solicitudReposicionRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.SolicitudReposicion.NO_ENCONTRADA));
    }

    private Ubicacion obtenerZonaEmpaqueActiva(Long zonaEmpaqueId) {
        Ubicacion zonaEmpaque = ubicacionRepository.findById(zonaEmpaqueId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.SolicitudReposicion.ZONA_NO_ENCONTRADA));
        if (!zonaEmpaque.getActivo() || zonaEmpaque.getTipo() != TipoUbicacion.ZONA_EMPAQUE) {
            throw new BusinessException(MensajesError.SolicitudReposicion.DESTINO_INVALIDO);
        }
        return zonaEmpaque;
    }

    private SolicitudResponseDTO convertirRespuesta(SolicitudReposicion solicitudReposicion) {
        return SolicitudResponseDTO.builder()
                .id(solicitudReposicion.getId())
                .productoId(solicitudReposicion.getProducto().getId())
                .productoNombre(solicitudReposicion.getProducto().getNombre())
                .cantidad(solicitudReposicion.getCantidad())
                .prioridad(solicitudReposicion.getPrioridad())
                .estado(solicitudReposicion.getEstado())
                .zonaDestinoId(solicitudReposicion.getZonaDestino().getId())
                .zonaDestinoNombre(solicitudReposicion.getZonaDestino().getNombre())
                .usuarioSolicitante(solicitudReposicion.getUsuarioSolicitante().getUsername())
                .usuarioResponsable(
                        solicitudReposicion.getUsuarioResponsable() == null
                                ? null
                                : solicitudReposicion.getUsuarioResponsable().getUsername()
                )
                .observaciones(solicitudReposicion.getObservaciones())
                .fechaCreacion(solicitudReposicion.getFechaCreacion())
                .build();
    }
}
