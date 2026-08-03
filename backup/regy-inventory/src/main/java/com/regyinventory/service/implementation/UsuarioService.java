package com.regyinventory.service.implementation;

import org.modelmapper.ModelMapper;
import com.regyinventory.dto.request.ActualizarUsuarioRequestDTO;
import com.regyinventory.dto.request.CambiarContrasenaRequestDTO;
import com.regyinventory.dto.request.CrearUsuarioRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.RolUsuarioResponseDTO;
import com.regyinventory.dto.response.UsuarioResponseDTO;
import com.regyinventory.entities.Rol;
import com.regyinventory.entities.Usuario;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IRolRepository;
import com.regyinventory.repository.IUsuarioRepository;
import com.regyinventory.service.contracts.IUsuarioService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UsuarioResponseDTO create(
            CrearUsuarioRequestDTO request
    ) {

        validarCreacion(request);

        Set<Rol> roles =
                buscarRoles(request.getRoleIds());

        Usuario usuario = Usuario.builder()
                .identificacion(
                        request.getIdentificacion().trim()
                )
                .nombre(
                        request.getNombre().trim()
                )
                .apellido(
                        request.getApellido().trim()
                )
                .correo(
                        normalizarCorreo(request.getCorreo())
                )
                .username(
                        normalizarUsername(request.getUsername())
                )
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .roles(roles)
                .build();

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        return convertirRespuesta(usuarioGuardado);
    }

    @Override
    public UsuarioResponseDTO findById(Long id) {

        return convertirRespuesta(
                buscarEntidad(id)
        );
    }

    @Override
    public PageResponseDTO<UsuarioResponseDTO> findAll(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    ) {

        Pageable pageable = PageableUtil.create(
                page,
                size,
                sortBy,
                direction
        );

        Page<Usuario> usuarios =
                usuarioRepository.findAll(pageable);

        return PageResponseDTO.fromPage(
                usuarios,
                this::convertirRespuesta
        );
    }

    @Override
    @Transactional
    public UsuarioResponseDTO update(
            Long id,
            ActualizarUsuarioRequestDTO request
    ) {

        Usuario usuario =
                buscarEntidad(id);

        validarActualizacion(id, request);

        Set<Rol> roles =
                buscarRoles(request.getRoleIds());

        usuario.setIdentificacion(
                request.getIdentificacion().trim()
        );

        usuario.setNombre(
                request.getNombre().trim()
        );

        usuario.setApellido(
                request.getApellido().trim()
        );

        usuario.setCorreo(
                normalizarCorreo(request.getCorreo())
        );

        usuario.setUsername(
                normalizarUsername(request.getUsername())
        );

        usuario.setRoles(roles);

        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);

        return convertirRespuesta(usuarioActualizado);
    }

    @Override
    @Transactional
    public void changePassword(
            Long id,
            CambiarContrasenaRequestDTO request
    ) {

        Usuario usuario =
                buscarEntidad(id);

        usuario.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO changeActiveStatus(
            Long id,
            boolean active,
            String authenticatedUsername
    ) {

        Usuario usuario =
                buscarEntidad(id);

        if (!active
                && usuario.getUsername()
                .equalsIgnoreCase(authenticatedUsername)) {

            throw new BusinessException(
                    MensajesError.Usuario.AUTODESACTIVACION
            );
        }

        if (Boolean.TRUE.equals(usuario.getActivo()) == active) {

            throw new BusinessException(
                    active
                            ? MensajesError.Usuario.YA_ACTIVO
                            : MensajesError.Usuario.YA_INACTIVO
            );
        }

        usuario.setActivo(active);

        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);

        return convertirRespuesta(usuarioActualizado);
    }

    private Usuario buscarEntidad(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format(
                                        MensajesError.Usuario.NO_EXISTE,
                                        id
                                )
                        )
                );
    }

    private void validarCreacion(
            CrearUsuarioRequestDTO request
    ) {

        String identificacion =
                request.getIdentificacion().trim();

        String correo =
                normalizarCorreo(request.getCorreo());

        String username =
                normalizarUsername(request.getUsername());

        if (usuarioRepository
                .existsByIdentificacion(identificacion)) {

            throw new BusinessException(
                    MensajesError.Usuario.IDENTIFICACION_DUPLICADA
            );
        }

        if (usuarioRepository.existsByCorreo(correo)) {

            throw new BusinessException(
                    MensajesError.Usuario.CORREO_DUPLICADO
            );
        }

        if (usuarioRepository.existsByUsername(username)) {

            throw new BusinessException(
                    MensajesError.Usuario.USERNAME_DUPLICADO
            );
        }
    }

    private void validarActualizacion(
            Long id,
            ActualizarUsuarioRequestDTO request
    ) {

        String identificacion =
                request.getIdentificacion().trim();

        String correo =
                normalizarCorreo(request.getCorreo());

        String username =
                normalizarUsername(request.getUsername());

        if (usuarioRepository
                .existsByIdentificacionAndIdNot(
                        identificacion,
                        id
                )) {

            throw new BusinessException(
                    MensajesError.Usuario
                            .OTRA_IDENTIFICACION_DUPLICADA
            );
        }

        if (usuarioRepository
                .existsByCorreoAndIdNot(
                        correo,
                        id
                )) {

            throw new BusinessException(
                    MensajesError.Usuario
                            .OTRO_CORREO_DUPLICADO
            );
        }

        if (usuarioRepository
                .existsByUsernameAndIdNot(
                        username,
                        id
                )) {

            throw new BusinessException(
                    MensajesError.Usuario
                            .OTRO_USERNAME_DUPLICADO
            );
        }
    }

    private Set<Rol> buscarRoles(
            Set<Long> roleIds
    ) {

        List<Rol> roles =
                rolRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {

            Set<Long> rolesEncontrados = roles.stream()
                    .map(Rol::getId)
                    .collect(Collectors.toSet());

            Set<Long> rolesFaltantes =
                    new HashSet<>(roleIds);

            rolesFaltantes.removeAll(
                    rolesEncontrados
            );

            throw new BusinessException(
                    String.format(
                            MensajesError.Usuario.ROLES_NO_EXISTEN,
                            rolesFaltantes
                    )
            );
        }

        boolean existenRolesInactivos =
                roles.stream()
                        .anyMatch(
                                rol ->
                                        !Boolean.TRUE.equals(
                                                rol.getActivo()
                                        )
                        );

        if (existenRolesInactivos) {

            throw new BusinessException(
                    MensajesError.Usuario.ROLES_INACTIVOS
            );
        }

        return new HashSet<>(roles);
    }

    private UsuarioResponseDTO convertirRespuesta(
            Usuario usuario
    ) {

        UsuarioResponseDTO response =
                modelMapper.map(
                        usuario,
                        UsuarioResponseDTO.class
                );

        Set<RolUsuarioResponseDTO> roles =
                usuario.getRoles()
                        .stream()
                        .map(rol ->
                                RolUsuarioResponseDTO.builder()
                                        .id(rol.getId())
                                        .nombre(
                                                rol.getNombre().name()
                                        )
                                        .descripcion(
                                                rol.getDescripcion()
                                        )
                                        .build()
                        )
                        .collect(Collectors.toSet());

        response.setRoles(roles);

        return response;
    }

    private String normalizarCorreo(
            String correo
    ) {

        return correo
                .trim()
                .toLowerCase();
    }

    private String normalizarUsername(
            String username
    ) {

        return username
                .trim()
                .toLowerCase();
    }
}