package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.entities.*;
import com.regyinventory.enums.*;
import com.regyinventory.exceptions.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.IInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService implements IInventarioService {
    private final IProductoRepository productoRepo;
    private final IUbicacionRepository ubicRepo;
    private final IZonaEmpaqueRepository zonaRepo;
    private final IIngresoStockRepository ingresoRepo;
    private final ILoteInventarioRepository loteRepo;
    private final IMovimientoInventarioRepository movRepo;
    private final OperacionSupport op;

    @Transactional
    public IngresoStockResponseDTO ingresar(IngresoStockRequestDTO r) {
        Producto p = producto(r.getProductoId());
        Destino d = destino(r.getTipoDestino(), r.getDestinoId());
        IngresoStock i = ingresoRepo.save(IngresoStock.builder().producto(p).cantidad(r.getCantidad()).tipoDestino(r.getTipoDestino()).ubicacion(d.u).zonaEmpaque(d.z).fechaVencimiento(r.getFechaVencimiento()).observaciones(l(r.getObservaciones())).usuario(op.usuarioActual()).build());
        loteRepo.save(LoteInventario.builder().producto(p).tipoDestino(r.getTipoDestino()).ubicacion(d.u).zonaEmpaque(d.z).cantidad(r.getCantidad()).fechaIngreso(LocalDateTime.now()).fechaVencimiento(r.getFechaVencimiento()).ingresoStock(i).build());
        op.log(TipoAccionLog.INGRESAR_STOCK, "IngresoStock", i.getId(), "Ingreso de " + r.getCantidad() + " unidades" );
        return ingresoDto(i, d.nombre);
    }

    @Transactional
    public MovimientoResponseDTO mover(MoverStockRequestDTO r) {
        if (r.getTipoOrigen() == r.getTipoDestino() && r.getOrigenId().equals(r.getDestinoId()))
            throw new BusinessException("El origen y destino no pueden ser iguales" );
        Producto p = producto(r.getProductoId());
        Destino origen = destino(r.getTipoOrigen(), r.getOrigenId()), dest = destino(r.getTipoDestino(), r.getDestinoId());
        transferir(p, r.getCantidad(), r.getTipoOrigen(), r.getOrigenId(), dest);
        MovimientoInventario m = movRepo.save(MovimientoInventario.builder().producto(p).cantidad(r.getCantidad()).tipoMovimiento(TipoMovimiento.TRASLADO).tipoOrigen(r.getTipoOrigen()).origenId(r.getOrigenId()).tipoDestino(r.getTipoDestino()).destinoId(r.getDestinoId()).observaciones(l(r.getObservaciones())).usuario(op.usuarioActual()).build());
        op.log(TipoAccionLog.MOVER_STOCK, "MovimientoInventario", m.getId(), "Movimiento de " + r.getCantidad() + " unidades" );
        return movDto(m, origen.nombre, dest.nombre);
    }

    public void transferir(Producto p, int cantidad, TipoDestino tipoOrigen, Long origenId, Destino dest) {
        List<LoteInventario> lotes = loteRepo.buscarLotesParaActualizar(p.getId(), tipoOrigen, origenId);
        lotes.sort(Comparator.comparing((LoteInventario x) -> x.getFechaVencimiento() == null ? LocalDate.MAX : x.getFechaVencimiento()).thenComparing(LoteInventario::getFechaIngreso));
        int disponible = lotes.stream().mapToInt(LoteInventario::getCantidad).sum();
        if (disponible < cantidad) throw new BusinessException("Stock insuficiente. Disponible: " + disponible);
        int restante = cantidad;
        for (LoteInventario l : lotes) {
            if (restante == 0) break;
            int toma = Math.min(restante, l.getCantidad());
            l.setCantidad(l.getCantidad() - toma);
            loteRepo.save(l);
            loteRepo.save(LoteInventario.builder().producto(p).tipoDestino(dest.tipo).ubicacion(dest.u).zonaEmpaque(dest.z).cantidad(toma).fechaIngreso(l.getFechaIngreso()).fechaVencimiento(l.getFechaVencimiento()).ingresoStock(l.getIngresoStock()).build());
            restante -= toma;
        }
    }

    public List<InventarioResponseDTO> existencias() {
        Map<String, InventarioResponseDTO> map = new LinkedHashMap<>();
        for (LoteInventario l : loteRepo.findAllByCantidadGreaterThan(0)) {
            String k = l.getProducto().getId() + "-" + l.getTipoDestino() + "-" + (l.getUbicacion() != null ? l.getUbicacion().getId() : l.getZonaEmpaque().getId());
            InventarioResponseDTO d = map.computeIfAbsent(k, z -> InventarioResponseDTO.builder().productoId(l.getProducto().getId()).numeroProducto(l.getProducto().getNumero()).nombreProducto(l.getProducto().getNombre()).tipoDestino(l.getTipoDestino()).destinoId(l.getUbicacion() != null ? l.getUbicacion().getId() : l.getZonaEmpaque().getId()).destinoNombre(l.getUbicacion() != null ? l.getUbicacion().getDeposito().getNombre() + " / " + l.getUbicacion().getNombre() : l.getZonaEmpaque().getNombre()).cantidad(0).stockMinimo(l.getProducto().getStockMinimo()).build());
            d.setCantidad(d.getCantidad() + l.getCantidad());
            d.setStockBajo(d.getCantidad() <= d.getStockMinimo());
        }
        return new ArrayList<>(map.values());
    }

    public List<InventarioResponseDTO> stockBajo() {
        return existencias().stream().filter(InventarioResponseDTO::getStockBajo).toList();
    }

    private Producto producto(Long id) {
        Producto p = productoRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado" ));
        if (!p.getActivo()) throw new BusinessException("Producto inactivo" );
        return p;
    }

    public Destino destino(TipoDestino t, Long id) {
        if (t == TipoDestino.UBICACION) {
            Ubicacion u = ubicRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada" ));
            if (!u.getActivo()) throw new BusinessException("Ubicación inactiva" );
            return new Destino(t, u, null, u.getDeposito().getNombre() + " / " + u.getNombre());
        }
        ZonaEmpaque z = zonaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada" ));
        if (!z.getActivo()) throw new BusinessException("Zona inactiva" );
        return new Destino(t, null, z, z.getNombre());
    }

    public record Destino(TipoDestino tipo, Ubicacion u, ZonaEmpaque z, String nombre) {
    }

    private String l(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private IngresoStockResponseDTO ingresoDto(IngresoStock x, String nom) {
        return IngresoStockResponseDTO.builder().id(x.getId()).productoId(x.getProducto().getId()).productoNombre(x.getProducto().getNombre()).cantidad(x.getCantidad()).tipoDestino(x.getTipoDestino()).destinoId(x.getUbicacion() != null ? x.getUbicacion().getId() : x.getZonaEmpaque().getId()).destinoNombre(nom).fechaVencimiento(x.getFechaVencimiento()).observaciones(x.getObservaciones()).usuario(x.getUsuario().getUsername()).fechaCreacion(x.getFechaCreacion()).build();
    }

    private MovimientoResponseDTO movDto(MovimientoInventario x, String o, String d) {
        return MovimientoResponseDTO.builder().id(x.getId()).productoId(x.getProducto().getId()).productoNombre(x.getProducto().getNombre()).cantidad(x.getCantidad()).tipoMovimiento(x.getTipoMovimiento()).tipoOrigen(x.getTipoOrigen()).origenId(x.getOrigenId()).origenNombre(o).tipoDestino(x.getTipoDestino()).destinoId(x.getDestinoId()).destinoNombre(d).observaciones(x.getObservaciones()).usuario(x.getUsuario().getUsername()).fechaCreacion(x.getFechaCreacion()).build();
    }
}
