package com.regyinventory.controllers;

import com.regyinventory.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Tag(
        name = "Pruebas",
        description = "Endpoints temporales para comprobar la autenticación"
)
@SecurityRequirement(name = "bearerAuth")
public class TestController {

    @GetMapping
    @Operation(
            summary = "Comprobar autenticación",
            description = "Endpoint protegido que requiere un JWT válido"
    )
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Endpoint protegido funcionando",
                        "REGY Inventory"
                )
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminTest() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permiso de administrador validado",
                        "Acceso autorizado"
                )
        );
    }
}