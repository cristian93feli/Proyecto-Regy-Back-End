package com.regyinventory.service.implementation;


import com.regyinventory.dto.request.ChangePasswordRequestDTO;
import com.regyinventory.dto.request.UserCreateRequestDTO;
import com.regyinventory.dto.request.UserUpdateRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UserResponseDTO;
import com.regyinventory.dto.response.UserRoleResponseDTO;
import com.regyinventory.entities.Role;
import com.regyinventory.entities.User;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IRoleRepository;
import com.regyinventory.repository.IUserRepository;
import com.regyinventory.service.contracts.IUserService;
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
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IGenericConverter converter;

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateRequestDTO request) {

        validateCreateRequest(request);

        Set<Role> roles = findRoles(request.getRoleIds());

        User user = User.builder()
                .identificacion(request.getIdentificacion().trim())
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .correo(normalizeEmail(request.getCorreo()))
                .username(normalizeUsername(request.getUsername()))
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Override
    public PageResponseDTO<UserResponseDTO> findAll(
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

        Page<User> users = userRepository.findAll(pageable);

        return PageResponseDTO.fromPage(
                users,
                this::toResponse
        );
    }

    private void validateCreateRequest(UserCreateRequestDTO request) {

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

    private Set<Role> findRoles(Set<Long> roleIds) {

        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {

            Set<Long> foundRoleIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingRoleIds = new HashSet<>(roleIds);
            missingRoleIds.removeAll(foundRoleIds);

            throw new BusinessException(
                    "No existen los roles con ID: " + missingRoleIds
            );
        }

        boolean hasInactiveRoles = roles.stream()
                .anyMatch(role -> !Boolean.TRUE.equals(role.getActivo()));

        if (hasInactiveRoles) {
            throw new BusinessException(
                    "No se pueden asignar roles inactivos"
            );
        }

        return new HashSet<>(roles);
    }

    private UserResponseDTO toResponse(User user) {

        UserResponseDTO response = converter.convert(
                user,
                UserResponseDTO.class
        );

        Set<UserRoleResponseDTO> roleResponses = user.getRoles()
                .stream()
                .map(role ->
                        UserRoleResponseDTO.builder()
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
    public UserResponseDTO update(
            Long id,
            UserUpdateRequestDTO request
    ) {

        User user = findEntityById(id);

        validateUpdateRequest(id, request);

        Set<Role> roles = findRoles(request.getRoleIds());

        user.setIdentificacion(request.getIdentificacion().trim());
        user.setNombre(request.getNombre().trim());
        user.setApellido(request.getApellido().trim());
        user.setCorreo(normalizeEmail(request.getCorreo()));
        user.setUsername(normalizeUsername(request.getUsername()));
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(
            Long id,
            ChangePasswordRequestDTO request
    ) {

        User user = findEntityById(id);

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDTO changeActiveStatus(
            Long id,
            boolean active,
            String authenticatedUsername
    ) {

        User user = findEntityById(id);

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

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    private User findEntityById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el usuario con ID " + id
                        )
                );
    }

    private void validateUpdateRequest(
            Long id,
            UserUpdateRequestDTO request
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