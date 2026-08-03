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
public class DepositoService implements IDepositoService {
    private final IDepositoRepository repo;
    private final IUbicacionRepository ubicRepo;
    private final OperacionSupport op;

    @Transactional
    public DepositoResponseDTO crear(CrearDepositoRequestDTO r) {
        validar(r.getNombre(), null);
        Deposito x = repo.save(Deposito.builder().nombre(r.getNombre().trim()).descripcion(l(r.getDescripcion())).build());
        op.log(TipoAccionLog.CREAR, "Deposito", x.getId(), "Depósito creado" );
        return d(x);
    }

    @Transactional
    public DepositoResponseDTO actualizar(Long id, CrearDepositoRequestDTO r) {
        Deposito x = g(id);
        validar(r.getNombre(), id);
        x.setNombre(r.getNombre().trim());
        x.setDescripcion(l(r.getDescripcion()));
        op.log(TipoAccionLog.ACTUALIZAR, "Deposito", id, "Depósito actualizado" );
        return d(repo.save(x));
    }

    public DepositoResponseDTO buscar(Long id) {
        return d(g(id));
    }

    public PageResponseDTO<DepositoResponseDTO> listar(Integer p, Integer s, String o, String dir) {
        return PageResponseDTO.fromPage(repo.findAll(PageableUtil.create(p, s, o, dir)), this::d);
    }

    @Transactional
    public DepositoResponseDTO estado(Long id, boolean a) {
        Deposito x = g(id);
        x.setActivo(a);
        return d(repo.save(x));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ubicRepo.findByDepositoId(id).isEmpty())
            throw new BusinessException("No se puede eliminar un depósito con ubicaciones" );
        repo.delete(g(id));
    }

    private void validar(String n, Long id) {
        if (id == null ? repo.existsByNombreIgnoreCase(n.trim()) : repo.existsByNombreIgnoreCaseAndIdNot(n.trim(), id))
            throw new BusinessException("Ya existe un depósito con ese nombre" );
    }

    private Deposito g(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado" ));
    }

    private DepositoResponseDTO d(Deposito x) {
        return DepositoResponseDTO.builder().id(x.getId()).nombre(x.getNombre()).descripcion(x.getDescripcion()).activo(x.getActivo()).build();
    }

    private String l(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
