package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.RoleResponseDTO;
import com.regyinventory.entities.Role;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IRoleRepository;
import com.regyinventory.service.contracts.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;

    @Override
    public List<RoleResponseDTO> findAll(boolean activeOnly) {

        return roleRepository.findAll()
                .stream()
                .filter(role ->
                        !activeOnly
                                || Boolean.TRUE.equals(role.getActivo())
                )
                .sorted(
                        Comparator.comparing(
                                role -> role.getNombre().name()
                        )
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RoleResponseDTO findById(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el rol con ID " + id
                        )
                );

        return toResponse(role);
    }

    private RoleResponseDTO toResponse(Role role) {

        return RoleResponseDTO.builder()
                .id(role.getId())
                .nombre(role.getNombre().name())
                .descripcion(role.getDescripcion())
                .activo(role.getActivo())
                .build();
    }
}