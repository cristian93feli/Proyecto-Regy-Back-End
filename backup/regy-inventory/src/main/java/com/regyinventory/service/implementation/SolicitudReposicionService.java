package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.entities.*;
import com.regyinventory.enums.*;
import com.regyinventory.exceptions.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudReposicionService implements ISolicitudReposicionService {
    private final ISolicitudReposicionRepository repo;
    private final IProductoRepository prodRepo;
    private final IZonaEmpaqueRepository zonaRepo;
    private final ILoteInventarioRepository loteRepo;
    private final IMovimientoInventarioRepository movRepo;
    private final InventarioService inventario;
    private final OperacionSupport op;

    @Transactional
    public SolicitudResponseDTO crear(CrearSolicitudRequestDTO r) {
        Producto p = prodRepo.findById(r.getProductoId()).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado" ));
        ZonaEmpaque z = zonaRepo.findById(r.getZonaDestinoId()).orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada" ));
        SolicitudReposicion s = repo.save(SolicitudReposicion.builder().producto(p).cantidad(r.getCantidad()).prioridad(r.getPrioridad()).zonaDestino(z).usuarioSolicitante(op.usuarioActual()).observaciones(r.getObservaciones()).build());
        op.log(TipoAccionLog.CREAR_SOLICITUD, "SolicitudReposicion", s.getId(), "Solicitud creada" );
        return d(s);
    }

    public PageResponseDTO<SolicitudResponseDTO> listar(EstadoSolicitud e, Integer p, Integer s) {
        Pageable pg = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "fechaCreacion" ));
        return PageResponseDTO.fromPage(e == null ? repo.findAll(pg) : repo.findByEstado(e, pg), this::d);
    }

    @Transactional
    public SolicitudResponseDTO completar(Long id, CompletarSolicitudRequestDTO r) {
        SolicitudReposicion s = g(id);
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) throw new BusinessException("La solicitud no está pendiente" );
        java.util.List<LoteInventario> candidatos = loteRepo.findAllByCantidadGreaterThan(0).stream().filter(l -> l.getProducto().getId().equals(s.getProducto().getId()) && l.getTipoDestino() == TipoDestino.UBICACION).sorted(java.util.Comparator.comparing((LoteInventario l) -> l.getFechaVencimiento() == null ? java.time.LocalDate.MAX : l.getFechaVencimiento()).thenComparing(LoteInventario::getFechaIngreso)).toList();
        int disponible = candidatos.stream().mapToInt(LoteInventario::getCantidad).sum();
        if (disponible < s.getCantidad()) throw new BusinessException("Stock insuficiente en depósitos" );
        int rest = s.getCantidad();
        for (LoteInventario l : candidatos) {
            if (rest == 0) break;
            int q = Math.min(rest, l.getCantidad());
            InventarioService.Destino dest = inventario.destino(TipoDestino.ZONA_EMPAQUE, s.getZonaDestino().getId());
            inventario.transferir(s.getProducto(), q, TipoDestino.UBICACION, l.getUbicacion().getId(), dest);
            movRepo.save(MovimientoInventario.builder().producto(s.getProducto()).cantidad(q).tipoMovimiento(TipoMovimiento.REPOSICION).tipoOrigen(TipoDestino.UBICACION).origenId(l.getUbicacion().getId()).tipoDestino(TipoDestino.ZONA_EMPAQUE).destinoId(s.getZonaDestino().getId()).observaciones(r == null ? null : r.getObservaciones()).usuario(op.usuarioActual()).build());
            rest -= q;
        }
        s.setEstado(EstadoSolicitud.COMPLETADA);
        s.setUsuarioResponsable(op.usuarioActual());
        repo.save(s);
        op.log(TipoAccionLog.COMPLETAR_SOLICITUD, "SolicitudReposicion", id, "Solicitud completada" );
        return d(s);
    }

    @Transactional
    public SolicitudResponseDTO cancelar(Long id) {
        SolicitudReposicion s = g(id);
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) throw new BusinessException("La solicitud no está pendiente" );
        s.setEstado(EstadoSolicitud.CANCELADA);
        s.setUsuarioResponsable(op.usuarioActual());
        repo.save(s);
        op.log(TipoAccionLog.CANCELAR_SOLICITUD, "SolicitudReposicion", id, "Solicitud cancelada" );
        return d(s);
    }

    private SolicitudReposicion g(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada" ));
    }

    private SolicitudResponseDTO d(SolicitudReposicion s) {
        return SolicitudResponseDTO.builder().id(s.getId()).productoId(s.getProducto().getId()).productoNombre(s.getProducto().getNombre()).cantidad(s.getCantidad()).prioridad(s.getPrioridad()).estado(s.getEstado()).zonaDestinoId(s.getZonaDestino().getId()).zonaDestinoNombre(s.getZonaDestino().getNombre()).usuarioSolicitante(s.getUsuarioSolicitante().getUsername()).usuarioResponsable(s.getUsuarioResponsable() == null ? null : s.getUsuarioResponsable().getUsername()).observaciones(s.getObservaciones()).fechaCreacion(s.getFechaCreacion()).build();
    }
}
