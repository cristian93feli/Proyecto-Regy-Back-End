package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.ActualizarConfiguracionRequestDTO;
import com.regyinventory.dto.response.ConfiguracionResponseDTO;
import java.util.List;

/** Define las operaciones de consulta y mantenimiento de parámetros funcionales. */
public interface IConfiguracionService {

    /** @return parámetros configurables del sistema */
    List<ConfiguracionResponseDTO> listar();

    /**
     * Actualiza el valor de un parámetro identificado por su clave.
     *
     * @param clave identificador único del parámetro
     * @param solicitudActualizacion nuevo valor y datos permitidos
     * @return configuración actualizada
     */
    ConfiguracionResponseDTO actualizar(
            String clave,
            ActualizarConfiguracionRequestDTO solicitudActualizacion
    );
}
