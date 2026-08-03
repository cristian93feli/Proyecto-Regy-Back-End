package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarConfiguracionRequestDTO;
import com.regyinventory.dto.response.ConfiguracionResponseDTO;
import com.regyinventory.entities.ConfiguracionSistema;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IConfiguracionSistemaRepository;
import com.regyinventory.service.contracts.IConfiguracionService;
import com.regyinventory.utils.constants.log.ConstantesLog;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionService implements IConfiguracionService {

    private final IConfiguracionSistemaRepository configuracionSistemaRepository;
    private final OperacionSupport operacionSupport;

    @Override
    public List<ConfiguracionResponseDTO> listar() {
        return configuracionSistemaRepository.findAll().stream()
                .map(this::convertirAConfiguracionResponse)
                .toList();
    }

    @Override
    @Transactional
    public ConfiguracionResponseDTO actualizar(
            String clave,
            ActualizarConfiguracionRequestDTO solicitudActualizacion
    ) {
        ConfiguracionSistema configuracionExistente = configuracionSistemaRepository.findByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Configuracion.NO_ENCONTRADA));

        configuracionExistente.setValor(solicitudActualizacion.getValor().trim());
        ConfiguracionSistema configuracionActualizada =
                configuracionSistemaRepository.save(configuracionExistente);

        operacionSupport.registrarLog(
                TipoAccionLog.CONFIGURAR,
                ConstantesLog.Entidad.CONFIGURACION_SISTEMA,
                configuracionActualizada.getId(),
                String.format(ConstantesLog.Detalle.CONFIGURACION_ACTUALIZADA, clave)
        );

        return convertirAConfiguracionResponse(configuracionActualizada);
    }

    private ConfiguracionResponseDTO convertirAConfiguracionResponse(
            ConfiguracionSistema configuracionSistema
    ) {
        return ConfiguracionResponseDTO.builder()
                .id(configuracionSistema.getId())
                .clave(configuracionSistema.getClave())
                .valor(configuracionSistema.getValor())
                .descripcion(configuracionSistema.getDescripcion())
                .build();
    }
}
