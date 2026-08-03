package com.regyinventory.controllers;

import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.LogResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.service.contracts.ILogService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.api.ValoresApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.security.ExpresionesSeguridad;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.LOGS)
@RequiredArgsConstructor
@Tag(name = DocumentacionApi.Log.TAG, description = DocumentacionApi.Log.DESCRIPCION_TAG)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class LogController {

    private final ILogService logService;

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.LOG_CONSULTAR)
    @Operation(summary = DocumentacionApi.Log.LISTAR)
    public ApiResponse<PageResponseDTO<LogResponseDTO>> listar(
            @RequestParam(defaultValue = ValoresApi.PAGINA_INICIAL) Integer numeroPagina,
            @RequestParam(defaultValue = ValoresApi.TAMANO_PAGINA_LOGS) Integer tamanoPagina
    ) {
        return ApiResponse.success(
                MensajesExito.Log.LISTADOS,
                logService.listar(numeroPagina, tamanoPagina)
        );
    }
}
