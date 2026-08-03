package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IUbicacionService {
    UbicacionResponseDTO crear(CrearUbicacionRequestDTO r);

    UbicacionResponseDTO actualizar(Long id, CrearUbicacionRequestDTO r);

    UbicacionResponseDTO buscar(Long id);

    PageResponseDTO<UbicacionResponseDTO> listar(Integer page, Integer size, String sortBy, String direction);

    UbicacionResponseDTO estado(Long id, boolean activo);

    void eliminar(Long id);
}
