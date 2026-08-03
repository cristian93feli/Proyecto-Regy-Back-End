package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IProductoService {
    ProductoResponseDTO crear(CrearProductoRequestDTO r);

    ProductoResponseDTO actualizar(Long id, ActualizarProductoRequestDTO r);

    ProductoResponseDTO buscar(Long id);

    PageResponseDTO<ProductoResponseDTO> listar(Integer page, Integer size, String sortBy, String direction);

    ProductoResponseDTO estado(Long id, boolean activo);

    void eliminar(Long id);
}
