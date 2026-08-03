package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.IngresoStockRequestDTO;
import com.regyinventory.dto.request.MoverStockRequestDTO;
import com.regyinventory.dto.response.IngresoStockResponseDTO;
import com.regyinventory.dto.response.InventarioResponseDTO;
import com.regyinventory.dto.response.MovimientoResponseDTO;
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
}
