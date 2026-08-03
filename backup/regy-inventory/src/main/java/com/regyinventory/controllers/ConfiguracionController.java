package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/configuracion" )
@RequiredArgsConstructor
public class ConfiguracionController {
    private final IConfiguracionService service;

    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURATION_READ')" )
    public ApiResponse<List<ConfiguracionResponseDTO>> listar() {
        return ApiResponse.success("Configuración consultada", service.listar());
    }

    @PutMapping("/{clave}" )
    @PreAuthorize("hasAuthority('SYSTEM_CONFIGURATION_UPDATE')" )
    public ApiResponse<ConfiguracionResponseDTO> actualizar(@PathVariable String clave, @Valid @RequestBody ActualizarConfiguracionRequestDTO r) {
        return ApiResponse.success("Configuración actualizada", service.actualizar(clave, r));
    }
}
