package com.regyinventory.service.implementation;


import com.regyinventory.dto.request.CambiarContrasenaRequestDTO;
import com.regyinventory.dto.request.CrearUsuarioRequestDTO;
import com.regyinventory.dto.request.ActualizarUsuarioRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UsuarioResponseDTO;
import com.regyinventory.dto.response.RolUsuarioResponseDTO;
import com.regyinventory.entities.Rol;
import com.regyinventory.entities.Usuario;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IRolRepository;
import com.regyinventory.repository.IUsuarioRepository;
import com.regyinventory.service.contracts.IUsuarioService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.mapper.IGenericConverter;
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

    private final IUsuarioRepository userRepository;
    private final IRolRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IGenericConverter converter;

    @Override
    @Transactional
    public UsuarioResponseDTO create(CrearUsuarioRequestDTO request) {

        validateCreateRequest(request);

        Set<Rol> rols = findRoles(request.getRoleIds());

        Usuario user = Usuario.builder()
                .identificacion(request.getIdentificacion().trim())
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .correo(normalizeEmail(request.getCorreo()))
                .username(normalizeUsername(request.getUsername()))
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(rols)
                .build();

        Usuario savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Override
    public UsuarioResponseDTO findById(Long id) {
        return toResponse(findEntityById(id));
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

        Page<Usuario> users = userRepository.findAll(pageable);

        return PageResponseDTO.fromPage(
                users,
                this::toResponse
        );
    }

    private void validateCreateRequest(CrearUsuarioRequestDTO request) {

        String identificacion = request.getIdentificacion().trim();
        String correo = normalizeEmail(request.getCorreo());
        String username = normalizeUsername(request.getUsername());

        if (userRepository.existsByIdentificacion(identificacion)) {
            throw new BusinessException(
                    "Ya existe un usuario con esa identificación"
            );
        }

        if (userRepository.existsByCorreo(correo)) {
            throw new BusinessException(
                    "Ya existe un usuario con ese correo"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(
                    "Ya existe un usuario con ese nombre de usuario"
            );
        }
    }

    private Set<Rol> findRoles(Set<Long> roleIds) {

        List<Rol> rols = roleRepository.findAllById(roleIds);

        if (rols.size() != roleIds.size()) {

            Set<Long> foundRoleIds = rols.stream()
                    .map(Rol::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingRoleIds = new HashSet<>(roleIds);
            missingRoleIds.removeAll(foundRoleIds);

            throw new BusinessException(
                    "No existen los roles con ID: " + missingRoleIds
            );
        }

        boolean hasInactiveRoles = rols.stream()
                .anyMatch(role -> !Boolean.TRUE.equals(role.getActivo()));

        if (hasInactiveRoles) {
            throw new BusinessException(
                    "No se pueden asignar roles inactivos"
            );
        }

        return new HashSet<>(rols);
    }

    private UsuarioResponseDTO toResponse(Usuario user) {

        UsuarioResponseDTO response = converter.convert(
                user,
                UsuarioResponseDTO.class
        );

        Set<RolUsuarioResponseDTO> roleResponses = user.getRoles()
                .stream()
                .map(role ->
                        RolUsuarioResponseDTO.builder()
                                .id(role.getId())
                                .nombre(role.getNombre().name())
                                .descripcion(role.getDescripcion())
                                .build()
                )
                .collect(Collectors.toSet());

        response.setRoles(roleResponses);

        return response;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }

    @Override
    @Transactional
    public UsuarioResponseDTO update(
            Long id,
            ActualizarUsuarioRequestDTO request
    ) {

        Usuario user = findEntityById(id);

        validateUpdateRequest(id, request);

        Set<Rol> rols = findRoles(request.getRoleIds());

        user.setIdentificacion(request.getIdentificacion().trim());
        user.setNombre(request.getNombre().trim());
        user.setApellido(request.getApellido().trim());
        user.setCorreo(normalizeEmail(request.getCorreo()));
        user.setUsername(normalizeUsername(request.getUsername()));
        user.setRoles(rols);

        Usuario updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(
            Long id,
            CambiarContrasenaRequestDTO request
    ) {

        Usuario user = findEntityById(id);

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO changeActiveStatus(
            Long id,
            boolean active,
            String authenticatedUsername
    ) {

        Usuario user = findEntityById(id);

        if (!active
                && user.getUsername()
                .equalsIgnoreCase(authenticatedUsername)) {

            throw new BusinessException(
                    "No puedes desactivar tu propio usuario"
            );
        }

        if (Boolean.TRUE.equals(user.getActivo()) == active) {
            throw new BusinessException(
                    active
                            ? "El usuario ya se encuentra activo"
                            : "El usuario ya se encuentra inactivo"
            );
        }

        user.setActivo(active);

        Usuario updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    private Usuario findEntityById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el usuario con ID " + id
                        )
                );
    }

    private void validateUpdateRequest(
            Long id,
            ActualizarUsuarioRequestDTO request
    ) {

        String identificacion =
                request.getIdentificacion().trim();

        String correo =
                normalizeEmail(request.getCorreo());

        String username =
                normalizeUsername(request.getUsername());

        if (userRepository.existsByIdentificacionAndIdNot(
                identificacion,
                id
        )) {
            throw new BusinessException(
                    "Ya existe otro usuario con esa identificación"
            );
        }

        if (userRepository.existsByCorreoAndIdNot(
                correo,
                id
        )) {
            throw new BusinessException(
                    "Ya existe otro usuario con ese correo"
            );
        }

        if (userRepository.existsByUsernameAndIdNot(
                username,
                id
        )) {
            throw new BusinessException(
                    "Ya existe otro usuario con ese nombre de usuario"
            );
        }
    }
}