package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarConfiguracionRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.ConfiguracionResponseDTO;
import com.regyinventory.service.contracts.IConfiguracionService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.security.ExpresionesSeguridad;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.CONFIGURACION)
@RequiredArgsConstructor
@Tag(name = DocumentacionApi.Configuracion.TAG, description = DocumentacionApi.Configuracion.DESCRIPCION_TAG)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class ConfiguracionController {

    private final IConfiguracionService configuracionService;

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.CONFIGURACION_CONSULTAR)
    @Operation(summary = DocumentacionApi.Configuracion.LISTAR)
    public ApiResponse<List<ConfiguracionResponseDTO>> listar() {
        return ApiResponse.success(
                MensajesExito.Configuracion.LISTADA,
                configuracionService.listar()
        );
    }

    @PutMapping(RutasApi.POR_CLAVE)
    @PreAuthorize(ExpresionesSeguridad.CONFIGURACION_ACTUALIZAR)
    @Operation(summary = DocumentacionApi.Configuracion.ACTUALIZAR)
    public ApiResponse<ConfiguracionResponseDTO> actualizar(
            @PathVariable String clave,
            @Valid @RequestBody ActualizarConfiguracionRequestDTO solicitudActualizacion
    ) {
        return ApiResponse.success(
                MensajesExito.Configuracion.ACTUALIZADA,
                configuracionService.actualizar(clave, solicitudActualizacion)
        );
    }
}
