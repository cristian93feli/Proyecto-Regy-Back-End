package com.regyinventory.controllers;

import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.RolResponseDTO;
import com.regyinventory.service.contracts.IRolService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.security.ExpresionesSeguridad;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RutasApi.ROLES)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Rol.TAG,
        description = DocumentacionApi.Rol.DESCRIPCION_TAG
)
@SecurityRequirement(
        name = DocumentacionApi.ESQUEMA_SEGURIDAD
)
public class RolController {

    private final IRolService rolService;

    @GetMapping
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Rol.LISTAR,
            description = DocumentacionApi.Rol.DESCRIPCION_LISTAR
    )
    public ResponseEntity<ApiResponse<List<RolResponseDTO>>> listar(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {

        List<RolResponseDTO> response =
                rolService.findAll(activeOnly);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Rol.LISTADOS,
                        response
                )
        );
    }

    @GetMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Rol.CONSULTAR,
            description = DocumentacionApi.Rol.DESCRIPCION_CONSULTAR
    )
    public ResponseEntity<ApiResponse<RolResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {

        RolResponseDTO response =
                rolService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Rol.ENCONTRADO,
                        response
                )
        );
    }
}