package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IDepositoService {
    DepositoResponseDTO crear(CrearDepositoRequestDTO r);

    DepositoResponseDTO actualizar(Long id, CrearDepositoRequestDTO r);

    DepositoResponseDTO buscar(Long id);

    PageResponseDTO<DepositoResponseDTO> listar(Integer page, Integer size, String sortBy, String direction);

    DepositoResponseDTO estado(Long id, boolean activo);

    void eliminar(Long id);
}
