package com.regyinventory.controllers;

import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.DashboardResponseDTO;
import com.regyinventory.service.contracts.IDashboardService;
import com.regyinventory.utils.constants.api.RutasApi;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.DASHBOARD)
@RequiredArgsConstructor
@Tag(name = DocumentacionApi.Dashboard.TAG, description = DocumentacionApi.Dashboard.DESCRIPCION_TAG)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.STOCK_CONSULTAR)
    @Operation(summary = DocumentacionApi.Dashboard.CONSULTAR)
    public ApiResponse<DashboardResponseDTO> consultarResumen() {
        return ApiResponse.success(
                MensajesExito.Dashboard.CONSULTADO,
                dashboardService.resumen()
        );
    }
}
