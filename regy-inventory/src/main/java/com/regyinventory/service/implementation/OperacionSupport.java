package com.regyinventory.service.implementation;

import com.regyinventory.entities.LogOperacion;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.entities.Usuario;
import com.regyinventory.enums.NombreRol;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.exceptions.UnauthorizedException;
import com.regyinventory.repository.ILogOperacionRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.repository.IUsuarioRepository;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperacionSupport {

    private final IUsuarioRepository usuarioRepository;
    private final IUbicacionRepository ubicacionRepository;
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

    /** Determina si el usuario autenticado trabaja bajo el alcance restringido de empaquetador. */
    public boolean usuarioAutenticadoEsEmpaquetador() {
        return obtenerUsuarioAutenticado().getRoles().stream()
                .anyMatch(rol -> rol.getNombre() == NombreRol.ROLE_PACKER);
    }

    /** Obtiene los identificadores de zonas de empaque que fueron asignadas al usuario autenticado. */
    public Set<Long> obtenerZonasAsignadasIds() {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        return ubicacionRepository
                .findDistinctByUsuariosResponsablesIdAndTipo(
                        usuarioAutenticado.getId(),
                        TipoUbicacion.ZONA_EMPAQUE
                )
                .stream()
                .filter(ubicacion -> Boolean.TRUE.equals(ubicacion.getActivo()))
                .map(Ubicacion::getId)
                .collect(Collectors.toSet());
    }

    /** Valida que un empaquetador solo opere sobre una zona que tenga asignada. */
    public void validarUbicacionPermitidaParaEmpaquetador(Long ubicacionId) {
        if (!usuarioAutenticadoEsEmpaquetador()) {
            return;
        }
        if (!obtenerZonasAsignadasIds().contains(ubicacionId)) {
            throw new UnauthorizedException(MensajesError.Operacion.UBICACION_NO_ASIGNADA);
        }
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
