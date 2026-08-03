package com.regyinventory.controllers;

import com.regyinventory.dto.response.*;
import com.regyinventory.service.contracts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs" )
@RequiredArgsConstructor
public class LogController {
    private final ILogService service;

    @GetMapping
    @PreAuthorize("hasAuthority('LOG_READ')" )
    public ApiResponse<PageResponseDTO<LogResponseDTO>> listar(@RequestParam(defaultValue = "0" ) Integer page, @RequestParam(defaultValue = "20" ) Integer size) {
        return ApiResponse.success("Logs consultados", service.listar(page, size));
    }
}
