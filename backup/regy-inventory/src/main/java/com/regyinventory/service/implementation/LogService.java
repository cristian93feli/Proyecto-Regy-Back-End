package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.*;
import com.regyinventory.entities.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService {
    private final ILogOperacionRepository repo;

    public PageResponseDTO<LogResponseDTO> listar(Integer p, Integer s) {
        return PageResponseDTO.fromPage(repo.findAll(PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "fechaCreacion" ))),
                x -> LogResponseDTO.builder().id(x.getId()).accion(x.getAccion()).entidad(x.getEntidad()).
                        entidadId(x.getEntidadId()).detalle(x.getDetalle()).
                        usuario(x.getUsuario() == null ? null : x.getUsuario().getUsername()).fechaCreacion(x.getFechaCreacion()).build());
    }
}
