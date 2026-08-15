package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.CompletarSolicitudRequestDTO;
import com.regyinventory.dto.request.CrearSolicitudRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.SolicitudResponseDTO;
import com.regyinventory.dto.response.SugerenciaReposicionDTO;
import java.util.List;
import com.regyinventory.enums.EstadoSolicitud;

/** Define el ciclo de vida de las solicitudes de reposición de zonas de empaque. */
public interface ISolicitudReposicionService {

    /** Registra una solicitud pendiente para abastecer una zona de empaque. */
    SolicitudResponseDTO crear(CrearSolicitudRequestDTO solicitudCreacion);

    /** Consulta solicitudes, opcionalmente filtradas por estado. */
    PageResponseDTO<SolicitudResponseDTO> listar(
            EstadoSolicitud estado,
            Integer numeroPagina,
            Integer tamanoPagina
    );

    /** Atiende total o parcialmente una solicitud y ejecuta la transferencia de inventario correspondiente. */
    SolicitudResponseDTO completar(
            Long solicitudId,
            CompletarSolicitudRequestDTO solicitudCompletado
    );

    /** Cancela una solicitud pendiente sin mover inventario. */
    SolicitudResponseDTO cancelar(Long solicitudId);

    /** Sugiere cajas de origen ordenadas por FEFO/FIFO para la solicitud indicada. */
    List<SugerenciaReposicionDTO> sugerencias(Long solicitudId);
}
