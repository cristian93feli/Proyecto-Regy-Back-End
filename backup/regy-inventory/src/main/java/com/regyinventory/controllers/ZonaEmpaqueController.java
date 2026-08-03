package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.IZonaEmpaqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zonas-empaque" )
@RequiredArgsConstructor
public class ZonaEmpaqueController {
    private final IZonaEmpaqueService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PACKING_ZONE_CREATE')" )
    public ResponseEntity<ApiResponse<ZonaEmpaqueResponseDTO>> crear(@Valid @RequestBody CrearZonaEmpaqueRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Creado correctamente", service.crear(r)));
    }

    @GetMapping("/{id}" )
    @PreAuthorize("hasAuthority('PACKING_ZONE_READ')" )
    public ApiResponse<ZonaEmpaqueResponseDTO> buscar(@PathVariable Long id) {
        return ApiResponse.success("Consultado correctamente", service.buscar(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PACKING_ZONE_READ')" )
    public ApiResponse<PageResponseDTO<ZonaEmpaqueResponseDTO>> listar(@RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "10" ) Integer size, @RequestParam(defaultValue = "id" ) String sortBy, @RequestParam(defaultValue = "ASC" ) String direction) {
        return ApiResponse.success("Consultados correctamente", service.listar(page, size, sortBy, direction));
    }

    @PutMapping("/{id}" )
    @PreAuthorize("hasAuthority('PACKING_ZONE_UPDATE')" )
    public ApiResponse<ZonaEmpaqueResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CrearZonaEmpaqueRequestDTO r) {
        return ApiResponse.success("Actualizado correctamente", service.actualizar(id, r));
    }

    @PatchMapping("/{id}/activar" )
    @PreAuthorize("hasAuthority('PACKING_ZONE_UPDATE')" )
    public ApiResponse<ZonaEmpaqueResponseDTO> activar(@PathVariable Long id) {
        return ApiResponse.success("Activado correctamente", service.estado(id, true));
    }

    @PatchMapping("/{id}/desactivar" )
    @PreAuthorize("hasAuthority('PACKING_ZONE_UPDATE')" )
    public ApiResponse<ZonaEmpaqueResponseDTO> desactivar(@PathVariable Long id) {
        return ApiResponse.success("Desactivado correctamente", service.estado(id, false));
    }

    @DeleteMapping("/{id}" )
    @PreAuthorize("hasAuthority('PACKING_ZONE_DELETE')" )
    public ApiResponse<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ApiResponse.success("Eliminado correctamente" );
    }
}
