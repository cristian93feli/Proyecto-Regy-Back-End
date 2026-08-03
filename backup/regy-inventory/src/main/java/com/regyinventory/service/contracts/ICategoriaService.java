package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.ActualizarCategoriaRequestDTO;
import com.regyinventory.dto.request.CrearCategoriaRequestDTO;
import com.regyinventory.dto.response.CategoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;

public interface ICategoriaService {

    CategoriaResponseDTO crear(
            CrearCategoriaRequestDTO request
    );

    CategoriaResponseDTO buscarPorId(Long id);

    PageResponseDTO<CategoriaResponseDTO> listar(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    );

    CategoriaResponseDTO actualizar(
            Long id,
            ActualizarCategoriaRequestDTO request
    );

    CategoriaResponseDTO cambiarEstado(
            Long id,
            boolean activo
    );

    void eliminar(Long id);
}