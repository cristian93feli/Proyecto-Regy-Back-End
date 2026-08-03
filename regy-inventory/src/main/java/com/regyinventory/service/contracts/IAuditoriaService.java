package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.CrearAuditoriaRequestDTO;
import com.regyinventory.dto.response.AuditoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;

/** Define las operaciones de conteo físico y ajuste auditado del inventario. */
public interface IAuditoriaService {

    /**
     * Registra el conteo físico de un producto en una ubicación y aplica el ajuste necesario.
     *
     * @param solicitudAuditoria datos del conteo realizado
     * @return auditoría registrada con la diferencia calculada
     */
    AuditoriaResponseDTO crear(CrearAuditoriaRequestDTO solicitudAuditoria);

    /**
     * Consulta las auditorías registradas de forma paginada.
     *
     * @param numeroPagina número de página basado en cero
     * @param tamanoPagina cantidad máxima de registros
     * @return página de auditorías
     */
    PageResponseDTO<AuditoriaResponseDTO> listar(Integer numeroPagina, Integer tamanoPagina);
}
