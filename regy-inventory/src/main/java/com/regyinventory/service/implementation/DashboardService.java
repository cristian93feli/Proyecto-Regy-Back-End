package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.DashboardResponseDTO;
import com.regyinventory.dto.response.InventarioResponseDTO;
import com.regyinventory.enums.EstadoSolicitud;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.ISolicitudReposicionRepository;
import com.regyinventory.service.contracts.IDashboardService;
import com.regyinventory.service.contracts.IInventarioService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {
    private final IProductoRepository productoRepository;
    private final ISolicitudReposicionRepository solicitudRepository;
    private final IInventarioService inventarioService;

    @Override
    public DashboardResponseDTO resumen() {
        List<InventarioResponseDTO> existencias = inventarioService.existencias();
        int stockTotal = existencias.stream().mapToInt(InventarioResponseDTO::getCantidad).sum();
        int stockDepositos = existencias.stream()
                .filter(existencia -> existencia.getTipoUbicacion() == TipoUbicacion.CAJA)
                .mapToInt(InventarioResponseDTO::getCantidad)
                .sum();
        int stockZonasEmpaque = existencias.stream()
                .filter(existencia -> existencia.getTipoUbicacion() == TipoUbicacion.ZONA_EMPAQUE)
                .mapToInt(InventarioResponseDTO::getCantidad)
                .sum();
        List<InventarioResponseDTO> productosConStockBajo = inventarioService.stockBajo();

        return DashboardResponseDTO.builder()
                .stockTotal(stockTotal)
                .stockDepositos(stockDepositos)
                .stockZonasEmpaque(stockZonasEmpaque)
                .productosActivos(productoRepository.findAll().stream().filter(producto -> producto.getActivo()).count())
                .productosStockBajo(productosConStockBajo.stream().map(InventarioResponseDTO::getProductoId).distinct().count())
                .solicitudesPendientes(solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE))
                .stockBajo(productosConStockBajo)
                .build();
    }
}
