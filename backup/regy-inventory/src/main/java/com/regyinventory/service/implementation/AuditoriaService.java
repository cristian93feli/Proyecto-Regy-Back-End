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

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService implements IAuditoriaService {
    private final IAuditoriaInventarioRepository repo;
    private final IProductoRepository prodRepo;
    private final ILoteInventarioRepository loteRepo;
    private final InventarioService inv;
    private final OperacionSupport op;

    @Transactional
    public AuditoriaResponseDTO crear(CrearAuditoriaRequestDTO r) {
        if (r.getMotivo() == MotivoAjuste.OTRO && (r.getMotivoOtro() == null || r.getMotivoOtro().isBlank()))
            throw new BusinessException("Debe especificar el motivo" );
        TipoDestino tipo = r.getTipoAuditoria() == TipoAuditoria.UBICACION ? TipoDestino.UBICACION : TipoDestino.ZONA_EMPAQUE;
        InventarioService.Destino dest = inv.destino(tipo, r.getDestinoId());
        AuditoriaInventario a = AuditoriaInventario.builder().tipoAuditoria(r.getTipoAuditoria()).destinoId(r.getDestinoId()).motivo(r.getMotivo()).motivoOtro(r.getMotivoOtro()).observaciones(r.getObservaciones()).usuario(op.usuarioActual()).build();
        for (ConteoAuditoriaRequestDTO c : r.getConteos()) {
            Producto p = prodRepo.findById(c.getProductoId()).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado" ));
            int sistema = loteRepo.stockDestino(p.getId(), tipo, r.getDestinoId());
            int dif = c.getCantidadContada() - sistema;
            if (dif > 0)
                loteRepo.save(LoteInventario.builder().producto(p).tipoDestino(tipo).ubicacion(dest.u()).zonaEmpaque(dest.z()).cantidad(dif).fechaIngreso(LocalDateTime.now()).build());
            else if (dif < 0) {
                List<LoteInventario> lotes = loteRepo.buscarLotesParaActualizar(p.getId(), tipo, r.getDestinoId());
                lotes.sort(Comparator.comparing((LoteInventario l) -> l.getFechaVencimiento() == null ? LocalDate.MAX : l.getFechaVencimiento()).thenComparing(LoteInventario::getFechaIngreso));
                int rest = -dif;
                for (LoteInventario l : lotes) {
                    int q = Math.min(rest, l.getCantidad());
                    l.setCantidad(l.getCantidad() - q);
                    loteRepo.save(l);
                    rest -= q;
                    if (rest == 0) break;
                }
            }
            DetalleAuditoria det = DetalleAuditoria.builder().auditoria(a).producto(p).cantidadSistema(sistema).cantidadContada(c.getCantidadContada()).diferencia(dif).build();
            a.getDetalles().add(det);
        }
        a = repo.save(a);
        op.log(TipoAccionLog.AUDITAR, "AuditoriaInventario", a.getId(), "Auditoría realizada en " + dest.nombre());
        return d(a, dest.nombre());
    }

    public PageResponseDTO<AuditoriaResponseDTO> listar(Integer p, Integer s) {
        return PageResponseDTO.fromPage(repo.findAll(PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "fechaCreacion" ))), x -> d(x, null));
    }

    private AuditoriaResponseDTO d(AuditoriaInventario a, String nombre) {
        return AuditoriaResponseDTO.builder().id(a.getId()).tipoAuditoria(a.getTipoAuditoria()).destinoId(a.getDestinoId()).destinoNombre(nombre).motivo(a.getMotivo()).motivoOtro(a.getMotivoOtro()).observaciones(a.getObservaciones()).usuario(a.getUsuario().getUsername()).fechaCreacion(a.getFechaCreacion()).detalles(a.getDetalles().stream().map(x -> DetalleAuditoriaResponseDTO.builder().productoId(x.getProducto().getId()).productoNombre(x.getProducto().getNombre()).cantidadSistema(x.getCantidadSistema()).cantidadContada(x.getCantidadContada()).diferencia(x.getDiferencia()).build()).toList()).build();
    }
}
