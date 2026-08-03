package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface ISolicitudReposicionService {
    SolicitudResponseDTO crear(CrearSolicitudRequestDTO r);

    PageResponseDTO<SolicitudResponseDTO> listar(com.regyinventory.enums.EstadoSolicitud estado, Integer page, Integer size);

    SolicitudResponseDTO completar(Long id, CompletarSolicitudRequestDTO r);

    SolicitudResponseDTO cancelar(Long id);
}
