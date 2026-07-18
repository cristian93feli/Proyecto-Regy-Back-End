package com.regyinventory.controllers;

import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.RoleResponseDTO;
import com.regyinventory.service.contracts.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(
        name = "Roles",
        description = "Consulta del catálogo de roles"
)
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final IRoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(
            summary = "Listar roles",
            description = "Obtiene el catálogo de roles disponibles"
    )
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> findAll(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {

        List<RoleResponseDTO> response =
                roleService.findAll(activeOnly);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roles consultados correctamente",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(
            summary = "Consultar rol",
            description = "Obtiene un rol por su identificador"
    )
    public ResponseEntity<ApiResponse<RoleResponseDTO>> findById(
            @PathVariable Long id
    ) {

        RoleResponseDTO response =
                roleService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rol encontrado",
                        response
                )
        );
    }
}