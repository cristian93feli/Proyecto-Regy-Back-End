package com.regyinventory.service.implementation;

import com.regyinventory.entities.*;
import com.regyinventory.enums.*;
import com.regyinventory.exceptions.*;
import com.regyinventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperacionSupport {
    private final IUsuarioRepository usuarioRepository;
    private final ILogOperacionRepository logRepository;

    public Usuario usuarioActual() {
        String u = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(u).orElseThrow(() -> new UnauthorizedException("Usuario autenticado no encontrado" ));
    }

    public void log(TipoAccionLog accion, String entidad, Long id, String detalle) {
        logRepository.save(LogOperacion.builder().accion(accion).entidad(entidad).entidadId(id).detalle(detalle).usuario(usuarioActual()).build());
    }
}
