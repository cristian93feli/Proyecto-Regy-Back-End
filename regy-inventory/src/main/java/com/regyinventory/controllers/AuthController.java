package com.regyinventory.controllers;

import com.regyinventory.dto.request.LoginRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.LoginResponseDTO;
import com.regyinventory.service.contracts.IAuthenticationService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.AUTENTICACION)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Autenticacion.TAG,
        description = DocumentacionApi.Autenticacion.DESCRIPCION_TAG
)
public class AuthController {

    private final IAuthenticationService authenticationService;

    @PostMapping(RutasApi.LOGIN)
    @Operation(
            summary = DocumentacionApi.Autenticacion.LOGIN,
            description = DocumentacionApi.Autenticacion.DESCRIPCION_LOGIN
    )
    @SecurityRequirements
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {

        LoginResponseDTO response =
                authenticationService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Autenticacion.LOGIN,
                        response
                )
        );
    }
}