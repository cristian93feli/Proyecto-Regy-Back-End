package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.CambiarContrasenaRequestDTO;
import com.regyinventory.dto.request.CrearUsuarioRequestDTO;
import com.regyinventory.dto.request.ActualizarUsuarioRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UsuarioResponseDTO;

public interface IUsuarioService {

    UsuarioResponseDTO create(CrearUsuarioRequestDTO request);

    UsuarioResponseDTO findById(Long id);

    PageResponseDTO<UsuarioResponseDTO> findAll(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    );

    UsuarioResponseDTO update(
            Long id,
            ActualizarUsuarioRequestDTO request
    );

    void changePassword(
            Long id,
            CambiarContrasenaRequestDTO request
    );

    UsuarioResponseDTO changeActiveStatus(
            Long id,
            boolean active,
            String authenticatedUsername
    );
}