package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.entities.*;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.exceptions.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfiguracionService implements IConfiguracionService {
    private final IConfiguracionSistemaRepository repo;
    private final OperacionSupport op;

    public List<ConfiguracionResponseDTO> listar() {
        return repo.findAll().stream().map(this::d).toList();
    }

    @Transactional
    public ConfiguracionResponseDTO actualizar(String clave, ActualizarConfiguracionRequestDTO r) {
        ConfiguracionSistema c = repo.findByClave(clave).orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada" ));
        c.setValor(r.getValor().trim());
        repo.save(c);
        op.log(TipoAccionLog.CONFIGURAR, "ConfiguracionSistema", c.getId(), "Configuración actualizada: " + clave);
        return d(c);
    }

    private ConfiguracionResponseDTO d(ConfiguracionSistema c) {
        return ConfiguracionResponseDTO.builder().id(c.getId()).clave(c.getClave()).valor(c.getValor()).descripcion(c.getDescripcion()).build();
    }
}
