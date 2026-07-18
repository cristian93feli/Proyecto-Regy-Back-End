package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.RolResponseDTO;
import com.regyinventory.entities.Rol;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IRolRepository;
import com.regyinventory.service.contracts.IRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService implements IRolService {

    private final IRolRepository roleRepository;

    @Override
    public List<RolResponseDTO> findAll(boolean activeOnly) {

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
    public RolResponseDTO findById(Long id) {

        Rol rol = roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el rol con ID " + id
                        )
                );

        return toResponse(rol);
    }

    private RolResponseDTO toResponse(Rol rol) {

        return RolResponseDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre().name())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .build();
    }
}