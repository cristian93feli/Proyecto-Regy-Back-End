package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IConfiguracionService {
    java.util.List<ConfiguracionResponseDTO> listar();

    ConfiguracionResponseDTO actualizar(String clave, ActualizarConfiguracionRequestDTO r);
}
