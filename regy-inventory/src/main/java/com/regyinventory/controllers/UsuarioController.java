package com.regyinventory.controllers;

import com.regyinventory.dto.request.CrearUsuarioRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UsuarioResponseDTO;
import com.regyinventory.service.contracts.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.regyinventory.dto.request.CambiarContrasenaRequestDTO;
import com.regyinventory.dto.request.ActualizarUsuarioRequestDTO;
import java.security.Principal;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(
        name = "Usuarios",
        description = "Administración de usuarios del sistema"
)
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final IUsuarioService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Operation(
            summary = "Crear usuario",
            description = "Crea un usuario y le asigna uno o varios roles"
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> create(
            @Valid @RequestBody CrearUsuarioRequestDTO request
    ) {

        UsuarioResponseDTO response = userService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Usuario creado correctamente",
                                response
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(
            summary = "Consultar usuario",
            description = "Obtiene un usuario por su identificador"
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> findById(
            @PathVariable Long id
    ) {

        UsuarioResponseDTO response = userService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Usuario encontrado",
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene los usuarios utilizando paginación y ordenamiento"
    )
    public ResponseEntity<ApiResponse<PageResponseDTO<UsuarioResponseDTO>>> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        PageResponseDTO<UsuarioResponseDTO> response =
                userService.findAll(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Usuarios consultados correctamente",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos y roles de un usuario"
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequestDTO request
    ) {

        UsuarioResponseDTO response =
                userService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Usuario actualizado correctamente",
                        response
                )
        );
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Operation(
            summary = "Cambiar contraseña",
            description = "Asigna una nueva contraseña cifrada al usuario"
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarContrasenaRequestDTO request
    ) {

        userService.changePassword(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Contraseña actualizada correctamente"
                )
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USER_ENABLE_DISABLE')")
    @Operation(
            summary = "Activar usuario",
            description = "Habilita el acceso de un usuario"
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> activate(
            @PathVariable Long id,
            Principal principal
    ) {

        UsuarioResponseDTO response =
                userService.changeActiveStatus(
                        id,
                        true,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Usuario activado correctamente",
                        response
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USER_ENABLE_DISABLE')")
    @Operation(
            summary = "Desactivar usuario",
            description = "Inhabilita el acceso de un usuario"
    )
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> deactivate(
            @PathVariable Long id,
            Principal principal
    ) {

        UsuarioResponseDTO response =
                userService.changeActiveStatus(
                        id,
                        false,
                        principal.getName()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Usuario desactivado correctamente",
                        response
                )
        );
    }
}