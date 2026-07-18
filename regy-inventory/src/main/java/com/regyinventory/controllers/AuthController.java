package com.regyinventory.controllers;

import com.regyinventory.dto.request.LoginRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.LoginResponseDTO;
import com.regyinventory.service.contracts.IAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Autenticación",
        description = "Operaciones públicas para iniciar sesión"
)
public class AuthController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales y devuelve un token JWT"
    )
    @SecurityRequirements
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        LoginResponseDTO response =
                authenticationService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Inicio de sesión exitoso",
                        response
                )
        );
    }
}