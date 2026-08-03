package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IZonaEmpaqueService {
    ZonaEmpaqueResponseDTO crear(CrearZonaEmpaqueRequestDTO r);

    ZonaEmpaqueResponseDTO actualizar(Long id, CrearZonaEmpaqueRequestDTO r);

    ZonaEmpaqueResponseDTO buscar(Long id);

    PageResponseDTO<ZonaEmpaqueResponseDTO> listar(Integer page, Integer size, String sortBy, String direction);

    ZonaEmpaqueResponseDTO estado(Long id, boolean activo);

    void eliminar(Long id);
}
