package com.regyinventory.service.contracts;

import com.regyinventory.dto.response.LogResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;

/** Proporciona acceso de solo lectura a la trazabilidad de acciones. */
public interface ILogService {

    /** Consulta los logs ordenados desde el más reciente. */
    PageResponseDTO<LogResponseDTO> listar(Integer numeroPagina, Integer tamanoPagina);
}
