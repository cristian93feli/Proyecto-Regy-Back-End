package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.*;
import com.regyinventory.enums.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {
    private final ILoteInventarioRepository lotes;
    private final IProductoRepository productos;
    private final ISolicitudReposicionRepository solicitudes;
    private final IInventarioService inventario;

    public DashboardResponseDTO resumen() {
        var ex = inventario.existencias();
        int total = ex.stream().mapToInt(InventarioResponseDTO::getCantidad).sum();
        int dep = ex.stream().filter(x -> x.getTipoDestino() == TipoDestino.UBICACION).mapToInt(InventarioResponseDTO::getCantidad).sum();
        int zon = total - dep;
        var bajos = inventario.stockBajo();
        return DashboardResponseDTO.builder().stockTotal(total).stockDepositos(dep).stockZonasEmpaque(zon).productosActivos(productos.findAll().stream().filter(x -> x.getActivo()).count()).productosStockBajo(bajos.stream().map(InventarioResponseDTO::getProductoId).distinct().count()).solicitudesPendientes(solicitudes.countByEstado(EstadoSolicitud.PENDIENTE)).stockBajo(bajos).build();
    }
}
