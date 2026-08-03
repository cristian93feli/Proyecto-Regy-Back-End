package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.IUbicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ubicaciones" )
@RequiredArgsConstructor
public class UbicacionController {
    private final IUbicacionService service;

    @PostMapping
    @PreAuthorize("hasAuthority('LOCATION_CREATE')" )
    public ResponseEntity<ApiResponse<UbicacionResponseDTO>> crear(@Valid @RequestBody CrearUbicacionRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Creado correctamente", service.crear(r)));
    }

    @GetMapping("/{id}" )
    @PreAuthorize("hasAuthority('LOCATION_READ')" )
    public ApiResponse<UbicacionResponseDTO> buscar(@PathVariable Long id) {
        return ApiResponse.success("Consultado correctamente", service.buscar(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LOCATION_READ')" )
    public ApiResponse<PageResponseDTO<UbicacionResponseDTO>> listar(@RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "10" ) Integer size, @RequestParam(defaultValue = "id" ) String sortBy, @RequestParam(defaultValue = "ASC" ) String direction) {
        return ApiResponse.success("Consultados correctamente", service.listar(page, size, sortBy, direction));
    }

    @PutMapping("/{id}" )
    @PreAuthorize("hasAuthority('LOCATION_UPDATE')" )
    public ApiResponse<UbicacionResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CrearUbicacionRequestDTO r) {
        return ApiResponse.success("Actualizado correctamente", service.actualizar(id, r));
    }

    @PatchMapping("/{id}/activar" )
    @PreAuthorize("hasAuthority('LOCATION_UPDATE')" )
    public ApiResponse<UbicacionResponseDTO> activar(@PathVariable Long id) {
        return ApiResponse.success("Activado correctamente", service.estado(id, true));
    }

    @PatchMapping("/{id}/desactivar" )
    @PreAuthorize("hasAuthority('LOCATION_UPDATE')" )
    public ApiResponse<UbicacionResponseDTO> desactivar(@PathVariable Long id) {
        return ApiResponse.success("Desactivado correctamente", service.estado(id, false));
    }

    @DeleteMapping("/{id}" )
    @PreAuthorize("hasAuthority('LOCATION_DELETE')" )
    public ApiResponse<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ApiResponse.success("Eliminado correctamente" );
    }
}
