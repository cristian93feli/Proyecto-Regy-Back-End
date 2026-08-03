package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ConteoAuditoriaRequestDTO;
import com.regyinventory.dto.request.CrearAuditoriaRequestDTO;
import com.regyinventory.dto.response.AuditoriaResponseDTO;
import com.regyinventory.dto.response.DetalleAuditoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.entities.AuditoriaInventario;
import com.regyinventory.entities.DetalleAuditoria;
import com.regyinventory.entities.LoteInventario;
import com.regyinventory.entities.Producto;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.enums.MotivoAjuste;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.TipoAuditoria;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IAuditoriaInventarioRepository;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.service.contracts.IAuditoriaService;
import com.regyinventory.utils.constants.api.ValoresApi;
import com.regyinventory.utils.constants.log.ConstantesLog;
import com.regyinventory.utils.constants.mensajes.MensajesError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService implements IAuditoriaService {
    private final IAuditoriaInventarioRepository auditoriaInventarioRepository;
    private final IProductoRepository productoRepository;
    private final ILoteInventarioRepository loteInventarioRepository;
    private final InventarioService inventarioService;
    private final OperacionSupport operacionSupport;

    @Override
    @Transactional
    public AuditoriaResponseDTO crear(CrearAuditoriaRequestDTO solicitudAuditoria) {
        validarMotivo(solicitudAuditoria);
        Ubicacion ubicacionAuditada = inventarioService.obtenerUbicacionActivaQueAdmiteInventario(solicitudAuditoria.getDestinoId());
        validarTipoAuditoria(solicitudAuditoria.getTipoAuditoria(), ubicacionAuditada);

        AuditoriaInventario auditoria = AuditoriaInventario.builder()
                .tipoAuditoria(solicitudAuditoria.getTipoAuditoria())
                .destinoId(ubicacionAuditada.getId())
                .motivo(solicitudAuditoria.getMotivo())
                .motivoOtro(solicitudAuditoria.getMotivoOtro())
                .observaciones(solicitudAuditoria.getObservaciones())
                .usuario(operacionSupport.obtenerUsuarioAutenticado())
                .build();

        for (ConteoAuditoriaRequestDTO conteo : solicitudAuditoria.getConteos()) {
            Producto producto = productoRepository.findById(conteo.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Inventario.PRODUCTO_NO_ENCONTRADO));
            int cantidadSistema = loteInventarioRepository.stockUbicacion(producto.getId(), ubicacionAuditada.getId());
            int diferencia = conteo.getCantidadContada() - cantidadSistema;
            aplicarDiferencia(producto, ubicacionAuditada, diferencia);

            DetalleAuditoria detalleAuditoria = DetalleAuditoria.builder()
                    .auditoria(auditoria)
                    .producto(producto)
                    .cantidadSistema(cantidadSistema)
                    .cantidadContada(conteo.getCantidadContada())
                    .diferencia(diferencia)
                    .build();
            auditoria.getDetalles().add(detalleAuditoria);
        }

        AuditoriaInventario auditoriaGuardada = auditoriaInventarioRepository.save(auditoria);
        operacionSupport.registrarLog(
                TipoAccionLog.AUDITAR,
                ConstantesLog.Entidad.AUDITORIA_INVENTARIO,
                auditoriaGuardada.getId(),
                String.format(ConstantesLog.Detalle.AUDITORIA_REALIZADA, ubicacionAuditada.getNombre())
        );
        return convertirRespuesta(auditoriaGuardada, ubicacionAuditada.getNombre());
    }

    @Override
    public PageResponseDTO<AuditoriaResponseDTO> listar(Integer numeroPagina, Integer tamanoPagina) {
        return PageResponseDTO.fromPage(
                auditoriaInventarioRepository.findAll(PageRequest.of(
                        numeroPagina,
                        tamanoPagina,
                        Sort.by(Sort.Direction.DESC, ValoresApi.CAMPO_FECHA_CREACION)
                )),
                auditoria -> convertirRespuesta(auditoria, null)
        );
    }

    private void validarMotivo(CrearAuditoriaRequestDTO solicitudAuditoria) {
        if (solicitudAuditoria.getMotivo() == MotivoAjuste.OTRO
                && (solicitudAuditoria.getMotivoOtro() == null || solicitudAuditoria.getMotivoOtro().isBlank())) {
            throw new BusinessException(MensajesError.Auditoria.MOTIVO_OTRO_REQUERIDO);
        }
    }

    private void validarTipoAuditoria(TipoAuditoria tipoAuditoria, Ubicacion ubicacion) {
        boolean tipoValido = tipoAuditoria == TipoAuditoria.CAJA
                ? ubicacion.getTipo() == TipoUbicacion.CAJA
                : ubicacion.getTipo() == TipoUbicacion.ZONA_EMPAQUE;
        if (!tipoValido) {
            throw new BusinessException(MensajesError.Auditoria.TIPO_UBICACION_INCOMPATIBLE);
        }
    }

    private void aplicarDiferencia(Producto producto, Ubicacion ubicacion, int diferencia) {
        if (diferencia > 0) {
            loteInventarioRepository.save(LoteInventario.builder()
                    .producto(producto)
                    .ubicacion(ubicacion)
                    .cantidad(diferencia)
                    .fechaIngreso(LocalDateTime.now())
                    .build());
            return;
        }
        if (diferencia >= 0) {
            return;
        }

        List<LoteInventario> lotesDisponibles = loteInventarioRepository.buscarLotesParaActualizar(
                producto.getId(),
                ubicacion.getId()
        );
        lotesDisponibles.sort(Comparator.comparing(
                (LoteInventario loteInventario) -> loteInventario.getFechaVencimiento() == null
                        ? LocalDate.MAX
                        : loteInventario.getFechaVencimiento()
        ).thenComparing(LoteInventario::getFechaIngreso));

        int cantidadPendiente = -diferencia;
        for (LoteInventario loteDisponible : lotesDisponibles) {
            int cantidadDescontada = Math.min(cantidadPendiente, loteDisponible.getCantidad());
            loteDisponible.setCantidad(loteDisponible.getCantidad() - cantidadDescontada);
            loteInventarioRepository.save(loteDisponible);
            cantidadPendiente -= cantidadDescontada;
            if (cantidadPendiente == 0) {
                break;
            }
        }
    }

    private AuditoriaResponseDTO convertirRespuesta(AuditoriaInventario auditoria, String destinoNombre) {
        return AuditoriaResponseDTO.builder()
                .id(auditoria.getId())
                .tipoAuditoria(auditoria.getTipoAuditoria())
                .destinoId(auditoria.getDestinoId())
                .destinoNombre(destinoNombre)
                .motivo(auditoria.getMotivo())
                .motivoOtro(auditoria.getMotivoOtro())
                .observaciones(auditoria.getObservaciones())
                .usuario(auditoria.getUsuario().getUsername())
                .fechaCreacion(auditoria.getFechaCreacion())
                .detalles(auditoria.getDetalles().stream()
                        .map(detalle -> DetalleAuditoriaResponseDTO.builder()
                                .productoId(detalle.getProducto().getId())
                                .productoNombre(detalle.getProducto().getNombre())
                                .cantidadSistema(detalle.getCantidadSistema())
                                .cantidadContada(detalle.getCantidadContada())
                                .diferencia(detalle.getDiferencia())
                                .build())
                        .toList())
                .build();
    }
}
