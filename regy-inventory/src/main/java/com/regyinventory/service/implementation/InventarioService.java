package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.IngresoStockRequestDTO;
import com.regyinventory.dto.request.MoverStockRequestDTO;
import com.regyinventory.dto.response.IngresoStockResponseDTO;
import com.regyinventory.dto.response.InventarioResponseDTO;
import com.regyinventory.dto.response.MovimientoResponseDTO;
import com.regyinventory.entities.IngresoStock;
import com.regyinventory.entities.LoteInventario;
import com.regyinventory.entities.MovimientoInventario;
import com.regyinventory.entities.Producto;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.TipoMovimiento;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IIngresoStockRepository;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IMovimientoInventarioRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.service.contracts.IInventarioService;
import com.regyinventory.utils.constants.api.ValoresApi;
import com.regyinventory.utils.constants.log.ConstantesLog;
import com.regyinventory.utils.constants.mensajes.MensajesError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService implements IInventarioService {

    private final IProductoRepository productoRepository;
    private final IUbicacionRepository ubicacionRepository;
    private final IIngresoStockRepository ingresoStockRepository;
    private final ILoteInventarioRepository loteInventarioRepository;
    private final IMovimientoInventarioRepository movimientoInventarioRepository;
    private final OperacionSupport operacionSupport;

    @Override
    @Transactional
    public IngresoStockResponseDTO ingresar(IngresoStockRequestDTO solicitudIngreso) {
        Producto producto = obtenerProductoActivo(solicitudIngreso.getProductoId());
        Ubicacion cajaDestino = obtenerUbicacionActivaQueAdmiteInventario(solicitudIngreso.getUbicacionDestinoId());
        if (cajaDestino.getTipo() != TipoUbicacion.CAJA && cajaDestino.getTipo() != TipoUbicacion.ZONA_EMPAQUE) {
            throw new BusinessException(MensajesError.Ubicacion.INGRESO_REQUIERE_CAJA);
        }

        IngresoStock ingresoStock = IngresoStock.builder()
                .producto(producto)
                .cantidad(solicitudIngreso.getCantidad())
                .ubicacion(cajaDestino)
                .fechaVencimiento(solicitudIngreso.getFechaVencimiento())
                .observaciones(limpiarTextoOpcional(solicitudIngreso.getObservaciones()))
                .usuario(operacionSupport.obtenerUsuarioAutenticado())
                .build();
        IngresoStock ingresoGuardado = ingresoStockRepository.save(ingresoStock);

        LoteInventario loteIngresado = LoteInventario.builder()
                .producto(producto)
                .ubicacion(cajaDestino)
                .cantidad(solicitudIngreso.getCantidad())
                .fechaIngreso(LocalDateTime.now())
                .fechaVencimiento(solicitudIngreso.getFechaVencimiento())
                .ingresoStock(ingresoGuardado)
                .build();
        loteInventarioRepository.save(loteIngresado);

        operacionSupport.registrarLog(
                TipoAccionLog.INGRESAR_STOCK,
                ConstantesLog.Entidad.INGRESO_STOCK,
                ingresoGuardado.getId(),
                ConstantesLog.Detalle.INGRESO_STOCK_REGISTRADO
        );
        return convertirIngresoRespuesta(ingresoGuardado);
    }

    @Override
    @Transactional
    public MovimientoResponseDTO mover(MoverStockRequestDTO solicitudMovimiento) {
        if (solicitudMovimiento.getUbicacionOrigenId().equals(solicitudMovimiento.getUbicacionDestinoId())) {
            throw new BusinessException(MensajesError.Inventario.ORIGEN_DESTINO_IGUALES);
        }
        Producto producto = obtenerProductoActivo(solicitudMovimiento.getProductoId());
        Ubicacion ubicacionOrigen = obtenerUbicacionActivaQueAdmiteInventario(solicitudMovimiento.getUbicacionOrigenId());
        Ubicacion ubicacionDestino = obtenerUbicacionActivaQueAdmiteInventario(solicitudMovimiento.getUbicacionDestinoId());

        transferirLotes(producto, solicitudMovimiento.getCantidad(), ubicacionOrigen, ubicacionDestino);

        MovimientoInventario movimientoInventario = MovimientoInventario.builder()
                .producto(producto)
                .cantidad(solicitudMovimiento.getCantidad())
                .tipoMovimiento(TipoMovimiento.TRASLADO)
                .ubicacionOrigen(ubicacionOrigen)
                .ubicacionDestino(ubicacionDestino)
                .observaciones(limpiarTextoOpcional(solicitudMovimiento.getObservaciones()))
                .usuario(operacionSupport.obtenerUsuarioAutenticado())
                .build();
        MovimientoInventario movimientoGuardado = movimientoInventarioRepository.save(movimientoInventario);

        operacionSupport.registrarLog(
                TipoAccionLog.MOVER_STOCK,
                ConstantesLog.Entidad.MOVIMIENTO_INVENTARIO,
                movimientoGuardado.getId(),
                ConstantesLog.Detalle.MOVIMIENTO_STOCK_REGISTRADO
        );
        return convertirMovimientoRespuesta(movimientoGuardado);
    }

    public void transferirLotes(
            Producto producto,
            int cantidadSolicitada,
            Ubicacion ubicacionOrigen,
            Ubicacion ubicacionDestino
    ) {
        List<LoteInventario> lotesDisponibles = loteInventarioRepository.buscarLotesParaActualizar(
                producto.getId(),
                ubicacionOrigen.getId()
        );
        ordenarLotesFefoFifo(lotesDisponibles);

        int cantidadDisponible = lotesDisponibles.stream().mapToInt(LoteInventario::getCantidad).sum();
        if (cantidadDisponible < cantidadSolicitada) {
            throw new BusinessException(String.format(MensajesError.Inventario.STOCK_INSUFICIENTE, cantidadDisponible));
        }

        int cantidadPendiente = cantidadSolicitada;
        for (LoteInventario loteDisponible : lotesDisponibles) {
            if (cantidadPendiente == 0) {
                break;
            }
            int cantidadTransferida = Math.min(cantidadPendiente, loteDisponible.getCantidad());
            loteDisponible.setCantidad(loteDisponible.getCantidad() - cantidadTransferida);
            loteInventarioRepository.save(loteDisponible);

            LoteInventario loteDestino = LoteInventario.builder()
                    .producto(producto)
                    .ubicacion(ubicacionDestino)
                    .cantidad(cantidadTransferida)
                    .fechaIngreso(loteDisponible.getFechaIngreso())
                    .fechaVencimiento(loteDisponible.getFechaVencimiento())
                    .ingresoStock(loteDisponible.getIngresoStock())
                    .build();
            loteInventarioRepository.save(loteDestino);
            cantidadPendiente -= cantidadTransferida;
        }
    }

    @Override
    public List<InventarioResponseDTO> existencias() {
        Map<String, InventarioResponseDTO> existenciasAgrupadas = new LinkedHashMap<>();
        for (LoteInventario loteInventario : loteInventarioRepository.findAllByCantidadGreaterThan(0)) {
            String claveAgrupacion = loteInventario.getProducto().getId() + ValoresApi.SEPARADOR_CLAVE_AGRUPACION + loteInventario.getUbicacion().getId();
            InventarioResponseDTO existencia = existenciasAgrupadas.computeIfAbsent(
                    claveAgrupacion,
                    clave -> construirExistenciaInicial(loteInventario)
            );
            existencia.setCantidad(existencia.getCantidad() + loteInventario.getCantidad());
            existencia.setStockBajo(existencia.getCantidad() <= existencia.getStockMinimo());
        }
        return new ArrayList<>(existenciasAgrupadas.values());
    }

    @Override
    public List<InventarioResponseDTO> stockBajo() {
        return existencias().stream().filter(InventarioResponseDTO::getStockBajo).toList();
    }

    public Ubicacion obtenerUbicacionActivaQueAdmiteInventario(Long ubicacionId) {
        Ubicacion ubicacion = ubicacionRepository.findById(ubicacionId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Ubicacion.NO_ENCONTRADA));
        if (!ubicacion.getActivo()) {
            throw new BusinessException(MensajesError.Ubicacion.INACTIVA);
        }
        if (ubicacion.getTipo() != TipoUbicacion.CAJA && ubicacion.getTipo() != TipoUbicacion.ZONA_EMPAQUE) {
            throw new BusinessException(MensajesError.Ubicacion.NO_ADMITE_INVENTARIO);
        }
        validarCadenaPadresActiva(ubicacion);
        return ubicacion;
    }

    private Producto obtenerProductoActivo(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Inventario.PRODUCTO_NO_ENCONTRADO));
        if (!producto.getActivo()) {
            throw new BusinessException(MensajesError.Inventario.PRODUCTO_INACTIVO);
        }
        return producto;
    }

    private void validarCadenaPadresActiva(Ubicacion ubicacion) {
        Ubicacion ubicacionPadre = ubicacion.getUbicacionPadre();
        while (ubicacionPadre != null) {
            if (!ubicacionPadre.getActivo()) {
                throw new BusinessException(MensajesError.Ubicacion.JERARQUIA_INACTIVA);
            }
            ubicacionPadre = ubicacionPadre.getUbicacionPadre();
        }
    }

    private void ordenarLotesFefoFifo(List<LoteInventario> lotesDisponibles) {
        lotesDisponibles.sort(
                Comparator.comparing(
                        (LoteInventario loteInventario) -> loteInventario.getFechaVencimiento() == null
                                ? LocalDate.MAX
                                : loteInventario.getFechaVencimiento()
                ).thenComparing(LoteInventario::getFechaIngreso)
        );
    }

    private InventarioResponseDTO construirExistenciaInicial(LoteInventario loteInventario) {
        Ubicacion ubicacion = loteInventario.getUbicacion();
        Ubicacion deposito = encontrarDeposito(ubicacion);
        return InventarioResponseDTO.builder()
                .productoId(loteInventario.getProducto().getId())
                .numeroProducto(loteInventario.getProducto().getNumero())
                .nombreProducto(loteInventario.getProducto().getNombre())
                .ubicacionId(ubicacion.getId())
                .ubicacionNombre(construirRutaUbicacion(ubicacion))
                .tipoUbicacion(ubicacion.getTipo())
                .depositoId(deposito == null ? null : deposito.getId())
                .depositoNombre(deposito == null ? null : deposito.getNombre())
                .cantidad(0)
                .stockMinimo(loteInventario.getProducto().getStockMinimo())
                .stockBajo(false)
                .build();
    }

    private IngresoStockResponseDTO convertirIngresoRespuesta(IngresoStock ingresoStock) {
        return IngresoStockResponseDTO.builder()
                .id(ingresoStock.getId())
                .productoId(ingresoStock.getProducto().getId())
                .productoNombre(ingresoStock.getProducto().getNombre())
                .cantidad(ingresoStock.getCantidad())
                .ubicacionDestinoId(ingresoStock.getUbicacion().getId())
                .ubicacionDestinoNombre(construirRutaUbicacion(ingresoStock.getUbicacion()))
                .tipoUbicacionDestino(ingresoStock.getUbicacion().getTipo())
                .fechaVencimiento(ingresoStock.getFechaVencimiento())
                .observaciones(ingresoStock.getObservaciones())
                .usuario(ingresoStock.getUsuario().getUsername())
                .fechaCreacion(ingresoStock.getFechaCreacion())
                .build();
    }

    private MovimientoResponseDTO convertirMovimientoRespuesta(MovimientoInventario movimientoInventario) {
        return MovimientoResponseDTO.builder()
                .id(movimientoInventario.getId())
                .productoId(movimientoInventario.getProducto().getId())
                .productoNombre(movimientoInventario.getProducto().getNombre())
                .cantidad(movimientoInventario.getCantidad())
                .tipoMovimiento(movimientoInventario.getTipoMovimiento())
                .ubicacionOrigenId(movimientoInventario.getUbicacionOrigen().getId())
                .ubicacionOrigenNombre(construirRutaUbicacion(movimientoInventario.getUbicacionOrigen()))
                .tipoUbicacionOrigen(movimientoInventario.getUbicacionOrigen().getTipo())
                .ubicacionDestinoId(movimientoInventario.getUbicacionDestino().getId())
                .ubicacionDestinoNombre(construirRutaUbicacion(movimientoInventario.getUbicacionDestino()))
                .tipoUbicacionDestino(movimientoInventario.getUbicacionDestino().getTipo())
                .observaciones(movimientoInventario.getObservaciones())
                .usuario(movimientoInventario.getUsuario().getUsername())
                .fechaCreacion(movimientoInventario.getFechaCreacion())
                .build();
    }

    private String construirRutaUbicacion(Ubicacion ubicacion) {
        List<String> nombresUbicacion = new ArrayList<>();
        Ubicacion ubicacionActual = ubicacion;
        while (ubicacionActual != null) {
            nombresUbicacion.add(0, ubicacionActual.getNombre());
            ubicacionActual = ubicacionActual.getUbicacionPadre();
        }
        return String.join(ValoresApi.SEPARADOR_JERARQUIA, nombresUbicacion);
    }

    private Ubicacion encontrarDeposito(Ubicacion ubicacion) {
        Ubicacion ubicacionActual = ubicacion;
        while (ubicacionActual != null && ubicacionActual.getTipo() != TipoUbicacion.DEPOSITO) {
            ubicacionActual = ubicacionActual.getUbicacionPadre();
        }
        return ubicacionActual;
    }

    private String limpiarTextoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
