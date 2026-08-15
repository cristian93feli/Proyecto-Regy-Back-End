package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.ActualizarInventarioRequestDTO;
import com.regyinventory.dto.request.IngresoStockRequestDTO;
import com.regyinventory.dto.request.MoverStockRequestDTO;
import com.regyinventory.dto.request.VentaProductoRequestDTO;
import com.regyinventory.dto.response.UbicacionResponseDTO;
import com.regyinventory.dto.response.IngresoStockResponseDTO;
import com.regyinventory.dto.response.InventarioResponseDTO;
import com.regyinventory.dto.response.MovimientoResponseDTO;
import com.regyinventory.dto.response.ProductoStockDisponibleDTO;
import java.util.List;

/**
 * Define las operaciones de ingreso, traslado y consulta del inventario físico.
 * El inventario solo puede existir en cajas y zonas de empaque activas.
 */
public interface IInventarioService {

    /**
     * Registra un ingreso de mercancía en una caja y crea el lote correspondiente.
     *
     * @param solicitudIngreso datos del producto, caja, cantidad y vencimiento
     * @return ingreso registrado
     */
    IngresoStockResponseDTO ingresar(IngresoStockRequestDTO solicitudIngreso);

    /**
     * Transfiere existencias entre ubicaciones habilitadas aplicando FEFO y FIFO.
     *
     * @param solicitudMovimiento datos del producto, origen, destino y cantidad
     * @return movimiento registrado
     */
    MovimientoResponseDTO mover(MoverStockRequestDTO solicitudMovimiento);

    /**
     * Consulta las existencias agrupadas por producto y ubicación.
     *
     * @return existencias actuales
     */
    List<InventarioResponseDTO> existencias();

    /**
     * Consulta las existencias cuya cantidad es menor o igual al stock mínimo.
     *
     * @return productos y ubicaciones con stock bajo
     */
    List<InventarioResponseDTO> stockBajo();

    /** Registra una salida por venta descontando lotes con FEFO/FIFO. */
    MovimientoResponseDTO vender(VentaProductoRequestDTO solicitudVenta);

    /**
     * Ajusta la cantidad actual de un producto en una ubicación y registra únicamente la diferencia.
     * Esta operación está destinada a administradores y conserva la trazabilidad mediante movimientos de ajuste.
     */
    MovimientoResponseDTO ajustar(ActualizarInventarioRequestDTO solicitudAjuste);

    /** Lista únicamente ubicaciones que tienen stock disponible del producto. */
    List<UbicacionResponseDTO> origenesDisponibles(Long productoId);

    /** Lista los productos que realmente tienen existencias disponibles para una salida por venta. */
    List<ProductoStockDisponibleDTO> productosDisponiblesParaVenta();
}
