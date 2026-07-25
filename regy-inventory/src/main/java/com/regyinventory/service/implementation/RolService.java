package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.RolResponseDTO;
import com.regyinventory.entities.Rol;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IRolRepository;
import com.regyinventory.service.contracts.IRolService;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService implements IRolService {

    private final IRolRepository rolRepository;

    @Override
    public List<RolResponseDTO> findAll(
            boolean activeOnly
    ) {

        return rolRepository.findAll()
                .stream()
                .filter(rol ->
                        !activeOnly
                                || Boolean.TRUE.equals(
                                rol.getActivo()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                rol ->
                                        rol.getNombre().name()
                        )
                )
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    public RolResponseDTO findById(Long id) {

        Rol rol = rolRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format(
                                        MensajesError.Rol.NO_EXISTE,
                                        id
                                )
                        )
                );

        return convertirRespuesta(rol);
    }

    private RolResponseDTO convertirRespuesta(
            Rol rol
    ) {

        return RolResponseDTO.builder()
                .id(rol.getId())
                .nombre(
                        rol.getNombre().name()
                )
                .descripcion(
                        rol.getDescripcion()
                )
                .activo(
                        rol.getActivo()
                )
                .build();
    }
}