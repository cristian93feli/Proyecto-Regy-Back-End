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
public class UbicacionService implements IUbicacionService {
    private final IUbicacionRepository repo;
    private final IDepositoRepository depRepo;
    private final ILoteInventarioRepository loteRepo;
    private final OperacionSupport op;

    @Transactional
    public UbicacionResponseDTO crear(CrearUbicacionRequestDTO r) {
        Deposito dep = dep(r.getDepositoId());
        validar(dep.getId(), r.getCodigo(), null);
        Ubicacion x = repo.save(Ubicacion.builder().codigo(r.getCodigo().trim().toUpperCase()).nombre(r.getNombre().trim()).descripcion(l(r.getDescripcion())).deposito(dep).build());
        op.log(TipoAccionLog.CREAR, "Ubicacion", x.getId(), "Ubicación creada" );
        return d(x);
    }

    @Transactional
    public UbicacionResponseDTO actualizar(Long id, CrearUbicacionRequestDTO r) {
        Ubicacion x = g(id);
        if (!x.getDeposito().getId().equals(r.getDepositoId()))
            throw new BusinessException("Una ubicación no puede cambiar de depósito" );
        validar(r.getDepositoId(), r.getCodigo(), id);
        x.setCodigo(r.getCodigo().trim().toUpperCase());
        x.setNombre(r.getNombre().trim());
        x.setDescripcion(l(r.getDescripcion()));
        return d(repo.save(x));
    }

    public UbicacionResponseDTO buscar(Long id) {
        return d(g(id));
    }

    public PageResponseDTO<UbicacionResponseDTO> listar(Integer p, Integer s, String o, String dir) {
        return PageResponseDTO.fromPage(repo.findAll(PageableUtil.create(p, s, o, dir)), this::d);
    }

    @Transactional
    public UbicacionResponseDTO estado(Long id, boolean a) {
        Ubicacion x = g(id);
        x.setActivo(a);
        return d(repo.save(x));
    }

    @Transactional
    public void eliminar(Long id) {
        if (loteRepo.findAllByCantidadGreaterThan(0).stream().anyMatch(l -> l.getUbicacion() != null && l.getUbicacion().getId().equals(id)))
            throw new BusinessException("No se puede eliminar una ubicación con stock" );
        repo.delete(g(id));
    }

    private Deposito dep(Long id) {
        Deposito d = depRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado" ));
        if (!d.getActivo()) throw new BusinessException("El depósito está inactivo" );
        return d;
    }

    private void validar(Long dep, String c, Long id) {
        if (id == null ? repo.existsByDepositoIdAndCodigoIgnoreCase(dep, c.trim()) : repo.existsByDepositoIdAndCodigoIgnoreCaseAndIdNot(dep, c.trim(), id))
            throw new BusinessException("Ya existe ese código en el depósito" );
    }

    private Ubicacion g(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada" ));
    }

    private UbicacionResponseDTO d(Ubicacion x) {
        return UbicacionResponseDTO.builder().id(x.getId()).codigo(x.getCodigo()).nombre(x.getNombre()).descripcion(x.getDescripcion()).depositoId(x.getDeposito().getId()).depositoNombre(x.getDeposito().getNombre()).activo(x.getActivo()).build();
    }

    private String l(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
