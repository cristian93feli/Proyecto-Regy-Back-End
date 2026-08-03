package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auditorias" )
@RequiredArgsConstructor
public class AuditoriaController {
    private final IAuditoriaService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('WAREHOUSE_AUDIT','LOCATION_AUDIT','PACKING_ZONE_AUDIT')" )
    public ResponseEntity<ApiResponse<AuditoriaResponseDTO>> crear(@Valid @RequestBody CrearAuditoriaRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Auditoría registrada", service.crear(r)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('WAREHOUSE_AUDIT','LOCATION_AUDIT','PACKING_ZONE_AUDIT')" )
    public ApiResponse<PageResponseDTO<AuditoriaResponseDTO>> listar(@RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "10" ) Integer size) {
        return ApiResponse.success("Auditorías consultadas", service.listar(page, size));
    }
}
