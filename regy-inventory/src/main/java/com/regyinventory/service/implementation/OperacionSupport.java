package com.regyinventory.service.implementation;

import com.regyinventory.entities.LogOperacion;
import com.regyinventory.entities.Usuario;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.exceptions.UnauthorizedException;
import com.regyinventory.repository.ILogOperacionRepository;
import com.regyinventory.repository.IUsuarioRepository;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperacionSupport {

    private final IUsuarioRepository usuarioRepository;
    private final ILogOperacionRepository logOperacionRepository;

    public Usuario obtenerUsuarioAutenticado() {
        String nombreUsuarioAutenticado = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByUsername(nombreUsuarioAutenticado)
                .orElseThrow(() -> new UnauthorizedException(
                        MensajesError.Operacion.USUARIO_AUTENTICADO_NO_ENCONTRADO
                ));
    }

    public void registrarLog(
            TipoAccionLog tipoAccion,
            String nombreEntidad,
            Long entidadId,
            String detalleOperacion
    ) {
        LogOperacion nuevoLog = LogOperacion.builder()
                .accion(tipoAccion)
                .entidad(nombreEntidad)
                .entidadId(entidadId)
                .detalle(detalleOperacion)
                .usuario(obtenerUsuarioAutenticado())
                .build();

        logOperacionRepository.save(nuevoLog);
    }
}
