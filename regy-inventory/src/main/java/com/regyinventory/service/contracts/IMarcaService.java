package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.ActualizarMarcaRequestDTO;
import com.regyinventory.dto.request.CrearMarcaRequestDTO;
import com.regyinventory.dto.response.MarcaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;

public interface IMarcaService {

    MarcaResponseDTO crear(CrearMarcaRequestDTO request);

    MarcaResponseDTO buscarPorId(Long id);

    PageResponseDTO<MarcaResponseDTO> listar(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    );

    MarcaResponseDTO actualizar(
            Long id,
            ActualizarMarcaRequestDTO request
    );

    MarcaResponseDTO cambiarEstado(
            Long id,
            boolean activo
    );

    void eliminar(Long id);
}