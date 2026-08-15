package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarInventarioRequestDTO;
import com.regyinventory.dto.request.IngresoStockRequestDTO;
import com.regyinventory.dto.request.MoverStockRequestDTO;
import com.regyinventory.dto.request.VentaProductoRequestDTO;
import com.regyinventory.dto.response.IngresoStockResponseDTO;
import com.regyinventory.dto.response.InventarioResponseDTO;
import com.regyinventory.dto.response.MovimientoResponseDTO;
import com.regyinventory.dto.response.ProductoStockDisponibleDTO;
import com.regyinventory.dto.response.UbicacionResponseDTO;
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
import com.regyinventory.utils.constants.numeros.Numeros;

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
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(cajaDestino.getId());
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
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(ubicacionOrigen.getId());
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(ubicacionDestino.getId());

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

    /** Registra una salida por venta y descuenta físicamente el stock siguiendo FEFO/FIFO. */
    @Override
    @Transactional
    public MovimientoResponseDTO vender(VentaProductoRequestDTO solicitudVenta) {
        Producto producto = obtenerProductoActivo(solicitudVenta.getProductoId());
        Ubicacion origen = obtenerUbicacionActivaQueAdmiteInventario(solicitudVenta.getUbicacionOrigenId());
        operacionSupport.validarUbicacionPermitidaParaEmpaquetador(origen.getId());
        descontarLotes(producto, solicitudVenta.getCantidad(), origen);
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto).cantidad(solicitudVenta.getCantidad()).tipoMovimiento(TipoMovimiento.SALIDA_VENTA)
                .ubicacionOrigen(origen).ubicacionDestino(null)
                .observaciones(limpiarTextoOpcional(solicitudVenta.getObservaciones()))
                .usuario(operacionSupport.obtenerUsuarioAutenticado()).build();
        return convertirMovimientoRespuesta(movimientoInventarioRepository.save(movimiento));
    }

    /**
     * Ajusta la cantidad de una existencia sin editar lotes históricos de forma directa.
     * Si aumenta, crea un lote administrativo; si disminuye, consume FEFO/FIFO.
     */
    @Override
    @Transactional
    public MovimientoResponseDTO ajustar(ActualizarInventarioRequestDTO solicitudAjuste) {
        Producto producto = obtenerProductoActivo(solicitudAjuste.getProductoId());
        Ubicacion ubicacion = obtenerUbicacionActivaQueAdmiteInventario(solicitudAjuste.getUbicacionId());
        int cantidadActual = loteInventarioRepository.stockUbicacion(producto.getId(), ubicacion.getId());
        int diferencia = solicitudAjuste.getNuevaCantidad() - cantidadActual;

        if (diferencia == Numeros.CERO) {
            throw new BusinessException(MensajesError.Inventario.AJUSTE_SIN_CAMBIOS);
        }

        TipoMovimiento tipoMovimiento;
        Ubicacion ubicacionOrigen = null;
        Ubicacion ubicacionDestino = null;

        if (diferencia > Numeros.CERO) {
            registrarLoteAjustePositivo(producto, ubicacion, diferencia);
            tipoMovimiento = TipoMovimiento.AJUSTE_POSITIVO;
            ubicacionDestino = ubicacion;
        } else {
            descontarLotes(producto, Math.abs(diferencia), ubicacion);
            tipoMovimiento = TipoMovimiento.AJUSTE_NEGATIVO;
            ubicacionOrigen = ubicacion;
        }

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .cantidad(Math.abs(diferencia))
                .tipoMovimiento(tipoMovimiento)
                .ubicacionOrigen(ubicacionOrigen)
                .ubicacionDestino(ubicacionDestino)
                .observaciones(limpiarTextoOpcional(solicitudAjuste.getObservaciones()))
                .usuario(operacionSupport.obtenerUsuarioAutenticado())
                .build();

        MovimientoInventario movimientoGuardado = movimientoInventarioRepository.save(movimiento);
        operacionSupport.registrarLog(
                TipoAccionLog.AJUSTAR,
                ConstantesLog.Entidad.MOVIMIENTO_INVENTARIO,
                movimientoGuardado.getId(),
                ConstantesLog.Detalle.AJUSTE_INVENTARIO_REGISTRADO
        );
        return convertirMovimientoRespuesta(movimientoGuardado);
    }

    /** Crea un lote administrativo por la diferencia positiva del ajuste. */
    private void registrarLoteAjustePositivo(Producto producto, Ubicacion ubicacion, int cantidad) {
        loteInventarioRepository.save(
                LoteInventario.builder()
                        .producto(producto)
                        .ubicacion(ubicacion)
                        .cantidad(cantidad)
                        .fechaIngreso(LocalDateTime.now())
                        .fechaVencimiento(null)
                        .ingresoStock(null)
                        .build()
        );
    }

    /**
     * Devuelve solo ubicaciones con stock y las ordena según el primer lote FEFO/FIFO disponible.
     */
    @Override
    public List<UbicacionResponseDTO> origenesDisponibles(Long productoId) {
        obtenerProductoActivo(productoId);
        java.util.Set<Long> zonasPermitidas = operacionSupport.usuarioAutenticadoEsEmpaquetador()
                ? operacionSupport.obtenerZonasAsignadasIds()
                : java.util.Set.of();

        Map<Long, Ubicacion> ubicacionesOrdenadas = new LinkedHashMap<>();
        for (LoteInventario lote : loteInventarioRepository.buscarLotesConStockOrdenados(productoId)) {
            Ubicacion ubicacion = lote.getUbicacion();
            if (operacionSupport.usuarioAutenticadoEsEmpaquetador()
                    && !zonasPermitidas.contains(ubicacion.getId())) {
                continue;
            }
            ubicacionesOrdenadas.putIfAbsent(ubicacion.getId(), ubicacion);
        }

        return ubicacionesOrdenadas.values().stream()
                .map(this::convertirUbicacionRespuesta)
                .toList();
    }

    /** Convierte una ubicación con stock al contrato resumido utilizado en selectores de inventario. */
    private UbicacionResponseDTO convertirUbicacionRespuesta(Ubicacion ubicacion) {
        Ubicacion deposito = encontrarDeposito(ubicacion);
        return UbicacionResponseDTO.builder()
                .id(ubicacion.getId())
                .codigo(ubicacion.getCodigo())
                .nombre(ubicacion.getNombre())
                .tipo(ubicacion.getTipo())
                .ubicacionPadreId(ubicacion.getUbicacionPadre() == null ? null : ubicacion.getUbicacionPadre().getId())
                .ubicacionPadreNombre(ubicacion.getUbicacionPadre() == null ? null : ubicacion.getUbicacionPadre().getNombre())
                .depositoId(deposito == null ? null : deposito.getId())
                .depositoNombre(deposito == null ? null : deposito.getNombre())
                .admiteInventario(true)
                .activo(ubicacion.getActivo())
                .build();
    }

    /** Construye el catálogo de venta exclusivamente con productos que tienen stock positivo. */
    @Override
    public List<ProductoStockDisponibleDTO> productosDisponiblesParaVenta() {
        List<Producto> productosDisponibles = operacionSupport.usuarioAutenticadoEsEmpaquetador()
                ? loteInventarioRepository.buscarProductosConStockEnUbicaciones(operacionSupport.obtenerZonasAsignadasIds())
                : loteInventarioRepository.buscarProductosConStock();

        return productosDisponibles.stream()
                .map(producto -> ProductoStockDisponibleDTO.builder()
                        .productoId(producto.getId())
                        .numero(producto.getNumero())
                        .nombre(producto.getNombre())
                        .codigoBarras(producto.getCodigoBarras())
                        .sku(producto.getSku())
                        .stockDisponible(calcularStockDisponiblePermitido(producto.getId()))
                        .build())
                .filter(producto -> producto.getStockDisponible() > Numeros.CERO)
                .sorted(Comparator.comparing(ProductoStockDisponibleDTO::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Calcula el stock visible del producto respetando el alcance de zona de un empaquetador. */
    private Integer calcularStockDisponiblePermitido(Long productoId) {
        if (!operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return loteInventarioRepository.stockTotalProducto(productoId);
        }
        return operacionSupport.obtenerZonasAsignadasIds().stream()
                .mapToInt(ubicacionId -> loteInventarioRepository.stockUbicacion(productoId, ubicacionId))
                .sum();
    }

    /** Descuenta lotes de una ubicación priorizando vencimiento y luego antigüedad de ingreso. */
    private void descontarLotes(Producto producto, int cantidadSolicitada, Ubicacion origen) {
        List<LoteInventario> lotes = loteInventarioRepository.buscarLotesParaActualizar(producto.getId(), origen.getId());
        ordenarLotesFefoFifo(lotes);
        int disponible = lotes.stream().mapToInt(LoteInventario::getCantidad).sum();
        if (disponible < cantidadSolicitada) throw new BusinessException(String.format(MensajesError.Inventario.STOCK_INSUFICIENTE, disponible));
        int pendiente = cantidadSolicitada;
        for (LoteInventario lote : lotes) {
            if (pendiente == Numeros.CERO) break;
            int descontado = Math.min(pendiente, lote.getCantidad());
            lote.setCantidad(lote.getCantidad() - descontado);
            loteInventarioRepository.save(lote);
            pendiente -= descontado;
        }
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
            if (cantidadPendiente == Numeros.CERO) {
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
        java.util.Set<Long> zonasPermitidas = operacionSupport.usuarioAutenticadoEsEmpaquetador()
                ? operacionSupport.obtenerZonasAsignadasIds()
                : java.util.Set.of();
        for (LoteInventario loteInventario : loteInventarioRepository.findAllByCantidadGreaterThan(Numeros.CERO)) {
            if (operacionSupport.usuarioAutenticadoEsEmpaquetador()
                    && !zonasPermitidas.contains(loteInventario.getUbicacion().getId())) {
                continue;
            }
            String claveAgrupacion = loteInventario.getProducto().getId() + ValoresApi.SEPARADOR_CLAVE_AGRUPACION + loteInventario.getUbicacion().getId();
            InventarioResponseDTO existencia = existenciasAgrupadas.computeIfAbsent(
                    claveAgrupacion,
                    clave -> construirExistenciaInicial(loteInventario)
            );
            existencia.setCantidad(existencia.getCantidad() + loteInventario.getCantidad());
            if (loteInventario.getFechaVencimiento() != null && (existencia.getFechaVencimiento() == null || loteInventario.getFechaVencimiento().isBefore(existencia.getFechaVencimiento()))) {
                existencia.setFechaVencimiento(loteInventario.getFechaVencimiento());
            }
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
                .fechaVencimiento(loteInventario.getFechaVencimiento())
                .cantidad(Numeros.CERO)
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
                .ubicacionOrigenId(movimientoInventario.getUbicacionOrigen() == null ? null : movimientoInventario.getUbicacionOrigen().getId())
                .ubicacionOrigenNombre(movimientoInventario.getUbicacionOrigen() == null ? null : construirRutaUbicacion(movimientoInventario.getUbicacionOrigen()))
                .tipoUbicacionOrigen(movimientoInventario.getUbicacionOrigen() == null ? null : movimientoInventario.getUbicacionOrigen().getTipo())
                .ubicacionDestinoId(movimientoInventario.getUbicacionDestino() == null ? null : movimientoInventario.getUbicacionDestino().getId())
                .ubicacionDestinoNombre(movimientoInventario.getUbicacionDestino() == null ? null : construirRutaUbicacion(movimientoInventario.getUbicacionDestino()))
                .tipoUbicacionDestino(movimientoInventario.getUbicacionDestino() == null ? null : movimientoInventario.getUbicacionDestino().getTipo())
                .observaciones(movimientoInventario.getObservaciones())
                .usuario(movimientoInventario.getUsuario().getUsername())
                .fechaCreacion(movimientoInventario.getFechaCreacion())
                .build();
    }

    private String construirRutaUbicacion(Ubicacion ubicacion) {
        List<String> nombresUbicacion = new ArrayList<>();
        Ubicacion ubicacionActual = ubicacion;
        while (ubicacionActual != null) {
            nombresUbicacion.add(Numeros.CERO, ubicacionActual.getNombre());
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
