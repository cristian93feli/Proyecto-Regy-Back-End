package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.IDepositoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depositos" )
@RequiredArgsConstructor
public class DepositoController {
    private final IDepositoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_CREATE')" )
    public ResponseEntity<ApiResponse<DepositoResponseDTO>> crear(@Valid @RequestBody CrearDepositoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Creado correctamente", service.crear(r)));
    }

    @GetMapping("/{id}" )
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')" )
    public ApiResponse<DepositoResponseDTO> buscar(@PathVariable Long id) {
        return ApiResponse.success("Consultado correctamente", service.buscar(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')" )
    public ApiResponse<PageResponseDTO<DepositoResponseDTO>> listar(@RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "10" ) Integer size, @RequestParam(defaultValue = "id" ) String sortBy, @RequestParam(defaultValue = "ASC" ) String direction) {
        return ApiResponse.success("Consultados correctamente", service.listar(page, size, sortBy, direction));
    }

    @PutMapping("/{id}" )
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')" )
    public ApiResponse<DepositoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CrearDepositoRequestDTO r) {
        return ApiResponse.success("Actualizado correctamente", service.actualizar(id, r));
    }

    @PatchMapping("/{id}/activar" )
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')" )
    public ApiResponse<DepositoResponseDTO> activar(@PathVariable Long id) {
        return ApiResponse.success("Activado correctamente", service.estado(id, true));
    }

    @PatchMapping("/{id}/desactivar" )
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')" )
    public ApiResponse<DepositoResponseDTO> desactivar(@PathVariable Long id) {
        return ApiResponse.success("Desactivado correctamente", service.estado(id, false));
    }

    @DeleteMapping("/{id}" )
    @PreAuthorize("hasAuthority('WAREHOUSE_DELETE')" )
    public ApiResponse<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ApiResponse.success("Eliminado correctamente" );
    }
}
