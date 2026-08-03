package com.regyinventory.controllers;

import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard" )
@RequiredArgsConstructor
public class DashboardController {
    private final IDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_READ')" )
    public ApiResponse<DashboardResponseDTO> resumen() {
        return ApiResponse.success("Dashboard consultado", service.resumen());
    }
}
