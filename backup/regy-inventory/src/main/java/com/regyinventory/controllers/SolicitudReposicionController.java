package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.enums.EstadoSolicitud;
import com.regyinventory.service.contracts.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes" )
@RequiredArgsConstructor
public class SolicitudReposicionController {
    private final ISolicitudReposicionService service;

    @PostMapping
    @PreAuthorize("hasAuthority('REPLENISHMENT_REQUEST_CREATE')" )
    public ResponseEntity<ApiResponse<SolicitudResponseDTO>> crear(@Valid @RequestBody CrearSolicitudRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Solicitud creada", service.crear(r)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPLENISHMENT_REQUEST_READ')" )
    public ApiResponse<PageResponseDTO<SolicitudResponseDTO>> listar(@RequestParam(required = false) EstadoSolicitud estado, @RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "10" ) Integer size) {
        return ApiResponse.success("Solicitudes consultadas", service.listar(estado, page, size));
    }

    @PostMapping("/{id}/completar" )
    @PreAuthorize("hasAuthority('REPLENISHMENT_REQUEST_COMPLETE')" )
    public ApiResponse<SolicitudResponseDTO> completar(@PathVariable Long id, @RequestBody(required = false) CompletarSolicitudRequestDTO r) {
        return ApiResponse.success("Solicitud completada", service.completar(id, r));
    }

    @PatchMapping("/{id}/cancelar" )
    @PreAuthorize("hasAuthority('REPLENISHMENT_REQUEST_CANCEL')" )
    public ApiResponse<SolicitudResponseDTO> cancelar(@PathVariable Long id) {
        return ApiResponse.success("Solicitud cancelada", service.cancelar(id));
    }
}
