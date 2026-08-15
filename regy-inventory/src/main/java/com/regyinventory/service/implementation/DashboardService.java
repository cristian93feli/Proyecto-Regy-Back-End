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
    private final OperacionSupport operacionSupport;

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
                .productosActivos(calcularProductosActivosVisibles(existencias))
                .productosStockBajo(productosConStockBajo.stream().map(InventarioResponseDTO::getProductoId).distinct().count())
                .solicitudesPendientes(calcularSolicitudesPendientesVisibles())
                .stockBajo(productosConStockBajo)
                .build();
    }
    /** Calcula productos visibles en dashboard sin exponer catálogos de otras zonas al empaquetador. */
    private long calcularProductosActivosVisibles(List<InventarioResponseDTO> existencias) {
        if (operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return existencias.stream().map(InventarioResponseDTO::getProductoId).distinct().count();
        }
        return productoRepository.findAll().stream().filter(producto -> producto.getActivo()).count();
    }

    /** Cuenta solicitudes pendientes respetando las zonas asignadas al empaquetador. */
    private long calcularSolicitudesPendientesVisibles() {
        if (operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return solicitudRepository.countByEstadoAndZonaDestinoIdIn(
                    EstadoSolicitud.PENDIENTE,
                    operacionSupport.obtenerZonasAsignadasIds()
            );
        }
        return solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE);
    }

}
