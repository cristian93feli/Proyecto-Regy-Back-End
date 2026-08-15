package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.CompletarSolicitudRequestDTO;
import com.regyinventory.dto.request.CrearSolicitudRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.SolicitudResponseDTO;
import com.regyinventory.dto.response.SugerenciaReposicionDTO;
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
import com.regyinventory.utils.constants.numeros.Numeros;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(zonaDestino.getId());

        SolicitudReposicion nuevaSolicitud = SolicitudReposicion.builder()
                .producto(producto)
                .cantidad(solicitudCreacion.getCantidad())
                .cantidadPendiente(solicitudCreacion.getCantidad())
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
    public PageResponseDTO<SolicitudResponseDTO> listar(
            EstadoSolicitud estado,
            Integer numeroPagina,
            Integer tamanoPagina
    ) {
        Pageable paginacion = PageRequest.of(
                numeroPagina,
                tamanoPagina,
                Sort.by(Sort.Direction.ASC, ValoresApi.ORDEN_POR_ID)
        );

        Page<SolicitudReposicion> paginaSolicitudes;
        if (operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            Set<Long> zonasAsignadas = operacionSupport.obtenerZonasAsignadasIds();
            paginaSolicitudes = estado == null
                    ? solicitudReposicionRepository.findByZonaDestinoIdIn(zonasAsignadas, paginacion)
                    : solicitudReposicionRepository.findByEstadoAndZonaDestinoIdIn(estado, zonasAsignadas, paginacion);
        } else {
            paginaSolicitudes = estado == null
                    ? solicitudReposicionRepository.findAll(paginacion)
                    : solicitudReposicionRepository.findByEstado(estado, paginacion);
        }

        return PageResponseDTO.fromPage(paginaSolicitudes, this::convertirRespuesta);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO completar(
            Long solicitudId,
            CompletarSolicitudRequestDTO solicitudCompletar
    ) {
        SolicitudReposicion solicitudReposicion = obtenerSolicitud(solicitudId);
        validarSolicitudPendiente(solicitudReposicion);

        if (solicitudCompletar == null || solicitudCompletar.getUbicacionOrigenId() == null) {
            throw new BusinessException(MensajesError.SolicitudReposicion.ORIGEN_REQUERIDO);
        }

        Ubicacion cajaOrigen = obtenerCajaOrigenValida(
                solicitudReposicion,
                solicitudCompletar.getUbicacionOrigenId()
        );
        validarCodigoBarras(solicitudReposicion.getProducto(), solicitudCompletar.getCodigoBarrasValidacion());

        Integer cantidadPendienteActual = obtenerCantidadPendiente(solicitudReposicion);
        Integer cantidadEnviar = solicitudCompletar.getCantidadEnviar();
        if (cantidadEnviar > cantidadPendienteActual) {
            throw new BusinessException(MensajesError.SolicitudReposicion.CANTIDAD_SUPERA_SOLICITUD);
        }

        int cantidadDisponible = loteInventarioRepository.stockUbicacion(
                solicitudReposicion.getProducto().getId(),
                cajaOrigen.getId()
        );
        if (cantidadEnviar > cantidadDisponible) {
            throw new BusinessException(MensajesError.SolicitudReposicion.CANTIDAD_SUPERA_STOCK_CAJA);
        }

        inventarioService.transferirLotes(
                solicitudReposicion.getProducto(),
                cantidadEnviar,
                cajaOrigen,
                solicitudReposicion.getZonaDestino()
        );

        MovimientoInventario movimientoReposicion = MovimientoInventario.builder()
                .producto(solicitudReposicion.getProducto())
                .cantidad(cantidadEnviar)
                .tipoMovimiento(TipoMovimiento.REPOSICION)
                .ubicacionOrigen(cajaOrigen)
                .ubicacionDestino(solicitudReposicion.getZonaDestino())
                .observaciones(solicitudCompletar.getObservaciones())
                .usuario(operacionSupport.obtenerUsuarioAutenticado())
                .build();
        movimientoInventarioRepository.save(movimientoReposicion);

        int cantidadPendiente = cantidadPendienteActual - cantidadEnviar;
        solicitudReposicion.setCantidadPendiente(cantidadPendiente);
        solicitudReposicion.setEstado(cantidadPendiente == Numeros.CERO
                ? EstadoSolicitud.COMPLETADA
                : EstadoSolicitud.PENDIENTE);
        solicitudReposicion.setUsuarioResponsable(operacionSupport.obtenerUsuarioAutenticado());
        solicitudReposicionRepository.save(solicitudReposicion);
        operacionSupport.registrarLog(
                TipoAccionLog.COMPLETAR_SOLICITUD,
                ConstantesLog.Entidad.SOLICITUD_REPOSICION,
                solicitudId,
                cantidadPendiente == Numeros.CERO
                        ? ConstantesLog.Detalle.SOLICITUD_COMPLETADA
                        : ConstantesLog.Detalle.SOLICITUD_COMPLETADA_PARCIAL
        );
        return convertirRespuesta(solicitudReposicion);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO cancelar(Long solicitudId) {
        SolicitudReposicion solicitudReposicion = obtenerSolicitud(solicitudId);
        validarSolicitudPendiente(solicitudReposicion);
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(solicitudReposicion.getZonaDestino().getId());

        solicitudReposicion.setEstado(EstadoSolicitud.CANCELADA);
        solicitudReposicion.setUsuarioResponsable(operacionSupport.obtenerUsuarioAutenticado());
        solicitudReposicionRepository.save(solicitudReposicion);
        operacionSupport.registrarLog(
                TipoAccionLog.CANCELAR_SOLICITUD,
                ConstantesLog.Entidad.SOLICITUD_REPOSICION,
                solicitudId,
                ConstantesLog.Detalle.SOLICITUD_CANCELADA
        );
        return convertirRespuesta(solicitudReposicion);
    }

    /** Construye sugerencias de cajas agrupando stock y priorizando FEFO, FIFO y código de caja. */
    @Override
    public List<SugerenciaReposicionDTO> sugerencias(Long solicitudId) {
        SolicitudReposicion solicitud = obtenerSolicitud(solicitudId);
        return construirSugerencias(solicitud.getProducto().getId());
    }

    /** Agrupa los lotes disponibles por caja y ordena primero los que deben salir por FEFO/FIFO. */
    private List<SugerenciaReposicionDTO> construirSugerencias(Long productoId) {
        List<LoteInventario> lotes = loteInventarioRepository.buscarLotesEnCajas(productoId).stream()
                .sorted(Comparator.comparing(
                        (LoteInventario lote) -> lote.getFechaVencimiento() == null
                                ? LocalDate.MAX
                                : lote.getFechaVencimiento()
                ).thenComparing(LoteInventario::getFechaIngreso)
                        .thenComparing(lote -> lote.getUbicacion().getCodigo(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<Long, SugerenciaReposicionDTO> agrupadas = new LinkedHashMap<>();
        for (LoteInventario lote : lotes) {
            SugerenciaReposicionDTO sugerencia = agrupadas.computeIfAbsent(
                    lote.getUbicacion().getId(),
                    ubicacionId -> SugerenciaReposicionDTO.builder()
                            .ubicacionId(ubicacionId)
                            .ubicacionCodigo(lote.getUbicacion().getCodigo())
                            .ubicacionNombre(lote.getUbicacion().getNombre())
                            .estanteNombre(lote.getUbicacion().getUbicacionPadre() == null
                                    ? null
                                    : lote.getUbicacion().getUbicacionPadre().getNombre())
                            .depositoNombre(obtenerNombreDeposito(lote.getUbicacion()))
                            .cantidadDisponible(Numeros.CERO)
                            .fechaVencimiento(lote.getFechaVencimiento())
                            .criterio(lote.getFechaVencimiento() == null ? ValoresApi.CRITERIO_FIFO : ValoresApi.CRITERIO_FEFO)
                            .build()
            );
            sugerencia.setCantidadDisponible(sugerencia.getCantidadDisponible() + lote.getCantidad());
            if (lote.getFechaVencimiento() != null
                    && (sugerencia.getFechaVencimiento() == null
                    || lote.getFechaVencimiento().isBefore(sugerencia.getFechaVencimiento()))) {
                sugerencia.setFechaVencimiento(lote.getFechaVencimiento());
                sugerencia.setCriterio(ValoresApi.CRITERIO_FEFO);
            }
        }
        return agrupadas.values().stream()
                .sorted(Comparator.comparing(
                        (SugerenciaReposicionDTO sugerencia) -> sugerencia.getFechaVencimiento() == null
                                ? LocalDate.MAX
                                : sugerencia.getFechaVencimiento()
                ).thenComparing(SugerenciaReposicionDTO::getUbicacionCodigo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Mantiene compatibilidad con solicitudes creadas antes de incorporar el seguimiento de cantidad pendiente. */
    private Integer obtenerCantidadPendiente(SolicitudReposicion solicitud) {
        return solicitud.getCantidadPendiente() == null
                ? solicitud.getCantidad()
                : solicitud.getCantidadPendiente();
    }

    /** Obtiene el depósito que contiene la caja sugerida sin modificar la jerarquía existente. */
    private String obtenerNombreDeposito(Ubicacion caja) {
        Ubicacion estante = caja.getUbicacionPadre();
        Ubicacion deposito = estante == null ? null : estante.getUbicacionPadre();
        return deposito == null ? null : deposito.getNombre();
    }

    /** Valida que la solicitud aún pueda ser procesada. */
    private void validarSolicitudPendiente(SolicitudReposicion solicitudReposicion) {
        if (solicitudReposicion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BusinessException(MensajesError.SolicitudReposicion.NO_PENDIENTE);
        }
    }

    /** Valida que la ubicación elegida sea una caja activa con existencias del producto solicitado. */
    private Ubicacion obtenerCajaOrigenValida(SolicitudReposicion solicitud, Long ubicacionOrigenId) {
        Ubicacion caja = ubicacionRepository.findById(ubicacionOrigenId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Ubicacion.NO_ENCONTRADA));
        if (!Boolean.TRUE.equals(caja.getActivo()) || caja.getTipo() != TipoUbicacion.CAJA) {
            throw new BusinessException(MensajesError.SolicitudReposicion.ORIGEN_INVALIDO);
        }
        if (loteInventarioRepository.stockUbicacion(solicitud.getProducto().getId(), caja.getId()) <= Numeros.CERO) {
            throw new BusinessException(MensajesError.SolicitudReposicion.ORIGEN_INVALIDO);
        }
        return caja;
    }

    /** Compara el código leído por el escáner con el código de barras maestro del producto. */
    private void validarCodigoBarras(Producto producto, String codigoBarrasValidacion) {
        if (codigoBarrasValidacion == null || codigoBarrasValidacion.isBlank()) {
            return;
        }
        if (producto.getCodigoBarras() == null
                || !producto.getCodigoBarras().equals(codigoBarrasValidacion.trim())) {
            throw new BusinessException(MensajesError.SolicitudReposicion.CODIGO_BARRAS_INVALIDO);
        }
    }

    private SolicitudReposicion obtenerSolicitud(Long solicitudId) {
        SolicitudReposicion solicitud = solicitudReposicionRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.SolicitudReposicion.NO_ENCONTRADA));
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(solicitud.getZonaDestino().getId());
        return solicitud;
    }

    private Ubicacion obtenerZonaEmpaqueActiva(Long zonaEmpaqueId) {
        Ubicacion zonaEmpaque = ubicacionRepository.findById(zonaEmpaqueId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.SolicitudReposicion.ZONA_NO_ENCONTRADA));
        if (!Boolean.TRUE.equals(zonaEmpaque.getActivo()) || zonaEmpaque.getTipo() != TipoUbicacion.ZONA_EMPAQUE) {
            throw new BusinessException(MensajesError.SolicitudReposicion.DESTINO_INVALIDO);
        }
        return zonaEmpaque;
    }

    /** Convierte una solicitud e incorpora la primera caja sugerida por FEFO/FIFO. */
    private SolicitudResponseDTO convertirRespuesta(SolicitudReposicion solicitudReposicion) {
        SugerenciaReposicionDTO sugerencia = solicitudReposicion.getEstado() == EstadoSolicitud.PENDIENTE
                ? construirSugerencias(solicitudReposicion.getProducto().getId()).stream().findFirst().orElse(null)
                : null;

        return SolicitudResponseDTO.builder()
                .id(solicitudReposicion.getId())
                .productoId(solicitudReposicion.getProducto().getId())
                .productoNumero(solicitudReposicion.getProducto().getNumero())
                .productoNombre(solicitudReposicion.getProducto().getNombre())
                .cantidad(solicitudReposicion.getCantidad())
                .cantidadPendiente(obtenerCantidadPendiente(solicitudReposicion))
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
                .sugerenciaUbicacionId(sugerencia == null ? null : sugerencia.getUbicacionId())
                .sugerenciaUbicacionCodigo(sugerencia == null ? null : sugerencia.getUbicacionCodigo())
                .sugerenciaUbicacionNombre(sugerencia == null ? null : sugerencia.getUbicacionNombre())
                .sugerenciaCantidadDisponible(sugerencia == null ? null : sugerencia.getCantidadDisponible())
                .sugerenciaFechaVencimiento(sugerencia == null ? null : sugerencia.getFechaVencimiento())
                .sugerenciaCriterio(sugerencia == null ? null : sugerencia.getCriterio())
                .build();
    }
}
