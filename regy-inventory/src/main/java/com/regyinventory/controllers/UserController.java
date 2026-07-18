package com.regyinventory.controllers;

import com.regyinventory.dto.request.UserCreateRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UserResponseDTO;
import com.regyinventory.service.contracts.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.regyinventory.dto.request.ChangePasswordRequestDTO;
import com.regyinventory.dto.request.UserUpdateRequestDTO;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "Usuarios",
        description = "Administración de usuarios del sistema"
)
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final IUserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Operation(
            summary = "Crear usuario",
            description = "Crea un usuario y le asigna uno o varios roles"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> create(
            @Valid @RequestBody UserCreateRequestDTO request
    ) {

        UserResponseDTO response = userService.create(request);

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
    public ResponseEntity<ApiResponse<UserResponseDTO>> findById(
            @PathVariable Long id
    ) {

        UserResponseDTO response = userService.findById(id);

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
    public ResponseEntity<ApiResponse<PageResponseDTO<UserResponseDTO>>> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        PageResponseDTO<UserResponseDTO> response =
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
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO request
    ) {

        UserResponseDTO response =
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
            @Valid @RequestBody ChangePasswordRequestDTO request
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
    public ResponseEntity<ApiResponse<UserResponseDTO>> activate(
            @PathVariable Long id,
            Principal principal
    ) {

        UserResponseDTO response =
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
    public ResponseEntity<ApiResponse<UserResponseDTO>> deactivate(
            @PathVariable Long id,
            Principal principal
    ) {

        UserResponseDTO response =
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