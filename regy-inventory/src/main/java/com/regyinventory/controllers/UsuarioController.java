package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarUsuarioRequestDTO;
import com.regyinventory.dto.request.CambiarContrasenaRequestDTO;
import com.regyinventory.dto.request.CrearUsuarioRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UsuarioResponseDTO;
import com.regyinventory.service.contracts.IUsuarioService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.security.ExpresionesSeguridad;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(RutasApi.USUARIOS)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Usuario.TAG,
        description = DocumentacionApi.Usuario.DESCRIPCION_TAG
)
@SecurityRequirement(
        name = DocumentacionApi.ESQUEMA_SEGURIDAD
)
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @PostMapping
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CREAR
    )
    @Operation(
            summary = DocumentacionApi.Usuario.CREAR,
            description = DocumentacionApi.Usuario.DESCRIPCION_CREAR
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> crear(
            @Valid @RequestBody CrearUsuarioRequestDTO request
    ) {

        UsuarioResponseDTO response =
                usuarioService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                MensajesExito.Usuario.CREADO,
                                response
                        )
                );
    }

    @GetMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Usuario.CONSULTAR,
            description = DocumentacionApi.Usuario.DESCRIPCION_CONSULTAR
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {

        UsuarioResponseDTO response =
                usuarioService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Usuario.ENCONTRADO,
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Usuario.LISTAR,
            description = DocumentacionApi.Usuario.DESCRIPCION_LISTAR
    )
    public ResponseEntity<ApiResponse<PageResponseDTO<UsuarioResponseDTO>>> listar(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {

        PageResponseDTO<UsuarioResponseDTO> response =
                usuarioService.findAll(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Usuario.LISTADOS,
                        response
                )
        );
    }

    @PutMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Usuario.ACTUALIZAR,
            description = DocumentacionApi.Usuario.DESCRIPCION_ACTUALIZAR
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequestDTO request
    ) {

        UsuarioResponseDTO response =
                usuarioService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Usuario.ACTUALIZADO,
                        response
                )
        );
    }

    @PatchMapping(RutasApi.CAMBIAR_CONTRASENA)
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Usuario.CAMBIAR_CONTRASENA,
            description = DocumentacionApi.Usuario.DESCRIPCION_CAMBIAR_CONTRASENA
    )
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(
            @PathVariable Long id,
            @Valid @RequestBody CambiarContrasenaRequestDTO request
    ) {

        usuarioService.changePassword(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Usuario.CONTRASENA_ACTUALIZADA
                )
        );
    }

    @PatchMapping(RutasApi.ACTIVAR)
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CAMBIAR_ESTADO
    )
    @Operation(
            summary = DocumentacionApi.Usuario.ACTIVAR,
            description = DocumentacionApi.Usuario.DESCRIPCION_ACTIVAR
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> activar(
            @PathVariable Long id,
            Principal principal
    ) {

        UsuarioResponseDTO response =
                usuarioService.changeActiveStatus(
                        id,
                        true,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Usuario.ACTIVADO,
                        response
                )
        );
    }

    @PatchMapping(RutasApi.DESACTIVAR)
    @PreAuthorize(
            ExpresionesSeguridad.USUARIO_CAMBIAR_ESTADO
    )
    @Operation(
            summary = DocumentacionApi.Usuario.DESACTIVAR,
            description = DocumentacionApi.Usuario.DESCRIPCION_DESACTIVAR
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> desactivar(
            @PathVariable Long id,
            Principal principal
    ) {

        UsuarioResponseDTO response =
                usuarioService.changeActiveStatus(
                        id,
                        false,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Usuario.DESACTIVADO,
                        response
                )
        );
    }
}