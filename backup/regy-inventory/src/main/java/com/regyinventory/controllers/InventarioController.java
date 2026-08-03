package com.regyinventory.controllers;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/inventario" )
@RequiredArgsConstructor
public class InventarioController {
    private final IInventarioService service;

    @PostMapping("/ingresos" )
    @PreAuthorize("hasAuthority('STOCK_RECEIVE')" )
    public ResponseEntity<ApiResponse<IngresoStockResponseDTO>> ingresar(@Valid @RequestBody IngresoStockRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Stock ingresado correctamente", service.ingresar(r)));
    }

    @PostMapping("/movimientos" )
    @PreAuthorize("hasAuthority('STOCK_MOVE')" )
    public ApiResponse<MovimientoResponseDTO> mover(@Valid @RequestBody MoverStockRequestDTO r) {
        return ApiResponse.success("Stock movido correctamente", service.mover(r));
    }

    @GetMapping("/existencias" )
    @PreAuthorize("hasAuthority('STOCK_READ')" )
    public ApiResponse<List<InventarioResponseDTO>> existencias() {
        return ApiResponse.success("Existencias consultadas", service.existencias());
    }

    @GetMapping("/stock-bajo" )
    @PreAuthorize("hasAuthority('STOCK_READ')" )
    public ApiResponse<List<InventarioResponseDTO>> bajos() {
        return ApiResponse.success("Stock bajo consultado", service.stockBajo());
    }
}
