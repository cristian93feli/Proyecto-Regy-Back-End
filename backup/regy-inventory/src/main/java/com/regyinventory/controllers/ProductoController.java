package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.IProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos" )
@RequiredArgsConstructor
public class ProductoController {
    private final IProductoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')" )
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> crear(@Valid @RequestBody CrearProductoRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Creado correctamente", service.crear(r)));
    }

    @GetMapping("/{id}" )
    @PreAuthorize("hasAuthority('PRODUCT_READ')" )
    public ApiResponse<ProductoResponseDTO> buscar(@PathVariable Long id) {
        return ApiResponse.success("Consultado correctamente", service.buscar(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')" )
    public ApiResponse<PageResponseDTO<ProductoResponseDTO>> listar(@RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "10" ) Integer size, @RequestParam(defaultValue = "id" ) String sortBy, @RequestParam(defaultValue = "ASC" ) String direction) {
        return ApiResponse.success("Consultados correctamente", service.listar(page, size, sortBy, direction));
    }

    @PutMapping("/{id}" )
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')" )
    public ApiResponse<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequestDTO r) {
        return ApiResponse.success("Actualizado correctamente", service.actualizar(id, r));
    }

    @PatchMapping("/{id}/activar" )
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')" )
    public ApiResponse<ProductoResponseDTO> activar(@PathVariable Long id) {
        return ApiResponse.success("Activado correctamente", service.estado(id, true));
    }

    @PatchMapping("/{id}/desactivar" )
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')" )
    public ApiResponse<ProductoResponseDTO> desactivar(@PathVariable Long id) {
        return ApiResponse.success("Desactivado correctamente", service.estado(id, false));
    }

    @DeleteMapping("/{id}" )
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')" )
    public ApiResponse<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ApiResponse.success("Eliminado correctamente" );
    }
}
