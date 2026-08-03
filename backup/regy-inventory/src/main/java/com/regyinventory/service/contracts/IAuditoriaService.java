package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IAuditoriaService {
    AuditoriaResponseDTO crear(CrearAuditoriaRequestDTO r);

    PageResponseDTO<AuditoriaResponseDTO> listar(Integer page, Integer size);
}
