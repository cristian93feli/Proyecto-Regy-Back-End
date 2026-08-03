package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.entities.*;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.exceptions.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.*;
import com.regyinventory.utils.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZonaEmpaqueService implements IZonaEmpaqueService {
    private final IZonaEmpaqueRepository repo;
    private final IUsuarioRepository userRepo;
    private final ILoteInventarioRepository loteRepo;
    private final OperacionSupport op;

    @Transactional
    public ZonaEmpaqueResponseDTO crear(CrearZonaEmpaqueRequestDTO r) {
        validar(r.getNombre(), null);
        ZonaEmpaque x = repo.save(ZonaEmpaque.builder().nombre(r.getNombre().trim()).descripcion(l(r.getDescripcion())).usuarioAsignado(user(r.getUsuarioAsignadoId())).build());
        op.log(TipoAccionLog.CREAR, "ZonaEmpaque", x.getId(), "Zona creada" );
        return d(x);
    }

    @Transactional
    public ZonaEmpaqueResponseDTO actualizar(Long id, CrearZonaEmpaqueRequestDTO r) {
        ZonaEmpaque x = g(id);
        validar(r.getNombre(), id);
        x.setNombre(r.getNombre().trim());
        x.setDescripcion(l(r.getDescripcion()));
        x.setUsuarioAsignado(user(r.getUsuarioAsignadoId()));
        return d(repo.save(x));
    }

    public ZonaEmpaqueResponseDTO buscar(Long id) {
        return d(g(id));
    }

    public PageResponseDTO<ZonaEmpaqueResponseDTO> listar(Integer p, Integer s, String o, String dir) {
        return PageResponseDTO.fromPage(repo.findAll(PageableUtil.create(p, s, o, dir)), this::d);
    }

    @Transactional
    public ZonaEmpaqueResponseDTO estado(Long id, boolean a) {
        ZonaEmpaque x = g(id);
        x.setActivo(a);
        return d(repo.save(x));
    }

    @Transactional
    public void eliminar(Long id) {
        if (loteRepo.findAllByCantidadGreaterThan(0).stream().anyMatch(l -> l.getZonaEmpaque() != null && l.getZonaEmpaque().getId().equals(id)))
            throw new BusinessException("No se puede eliminar una zona con stock" );
        repo.delete(g(id));
    }

    private Usuario user(Long id) {
        return id == null ? null : userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado" ));
    }

    private void validar(String n, Long id) {
        if (id == null ? repo.existsByNombreIgnoreCase(n.trim()) : repo.existsByNombreIgnoreCaseAndIdNot(n.trim(), id))
            throw new BusinessException("Ya existe una zona con ese nombre" );
    }

    private ZonaEmpaque g(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada" ));
    }

    private ZonaEmpaqueResponseDTO d(ZonaEmpaque x) {
        return ZonaEmpaqueResponseDTO.builder().id(x.getId()).nombre(x.getNombre()).descripcion(x.getDescripcion()).usuarioAsignadoId(x.getUsuarioAsignado() == null ? null : x.getUsuarioAsignado().getId()).usuarioAsignadoNombre(x.getUsuarioAsignado() == null ? null : x.getUsuarioAsignado().getNombre() + " " + x.getUsuarioAsignado().getApellido()).activo(x.getActivo()).build();
    }

    private String l(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
