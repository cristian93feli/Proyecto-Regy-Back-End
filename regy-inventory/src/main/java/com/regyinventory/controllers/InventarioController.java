package com.regyinventory.controllers;

import com.regyinventory.dto.request.IngresoStockRequestDTO;
import com.regyinventory.dto.request.MoverStockRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.IngresoStockResponseDTO;
import com.regyinventory.dto.response.InventarioResponseDTO;
import com.regyinventory.dto.response.MovimientoResponseDTO;
import com.regyinventory.service.contracts.IInventarioService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.INVENTARIO)
@RequiredArgsConstructor
@Tag(name = DocumentacionApi.Inventario.TAG, description = DocumentacionApi.Inventario.DESCRIPCION_TAG)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class InventarioController {

    private final IInventarioService inventarioService;

    @PostMapping(RutasApi.INGRESOS)
    @PreAuthorize(ExpresionesSeguridad.STOCK_RECIBIR)
    @Operation(summary = DocumentacionApi.Inventario.INGRESAR, description = DocumentacionApi.Inventario.DESCRIPCION_INGRESAR)
    public ResponseEntity<ApiResponse<IngresoStockResponseDTO>> ingresar(
            @Valid @RequestBody IngresoStockRequestDTO solicitudIngreso
    ) {
        IngresoStockResponseDTO ingresoRegistrado = inventarioService.ingresar(solicitudIngreso);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MensajesExito.Inventario.INGRESADO, ingresoRegistrado));
    }

    @PostMapping(RutasApi.MOVIMIENTOS)
    @PreAuthorize(ExpresionesSeguridad.STOCK_MOVER)
    @Operation(summary = DocumentacionApi.Inventario.MOVER, description = DocumentacionApi.Inventario.DESCRIPCION_MOVER)
    public ApiResponse<MovimientoResponseDTO> mover(
            @Valid @RequestBody MoverStockRequestDTO solicitudMovimiento
    ) {
        return ApiResponse.success(
                MensajesExito.Inventario.MOVIDO,
                inventarioService.mover(solicitudMovimiento)
        );
    }

    @GetMapping(RutasApi.EXISTENCIAS)
    @PreAuthorize(ExpresionesSeguridad.STOCK_CONSULTAR)
    @Operation(summary = DocumentacionApi.Inventario.EXISTENCIAS)
    public ApiResponse<List<InventarioResponseDTO>> consultarExistencias() {
        return ApiResponse.success(
                MensajesExito.Inventario.EXISTENCIAS,
                inventarioService.existencias()
        );
    }

    @GetMapping(RutasApi.STOCK_BAJO)
    @PreAuthorize(ExpresionesSeguridad.STOCK_CONSULTAR)
    @Operation(summary = DocumentacionApi.Inventario.STOCK_BAJO)
    public ApiResponse<List<InventarioResponseDTO>> consultarStockBajo() {
        return ApiResponse.success(
                MensajesExito.Inventario.STOCK_BAJO,
                inventarioService.stockBajo()
        );
    }
}
