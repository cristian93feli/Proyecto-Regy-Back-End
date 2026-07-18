package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarCategoriaRequestDTO;
import com.regyinventory.dto.request.CrearCategoriaRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.CategoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.service.contracts.ICategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(
        name = "Categorías",
        description = "Administración del catálogo de categorías"
)
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    private final ICategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría"
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> crear(
            @Valid @RequestBody CrearCategoriaRequestDTO request
    ) {

        CategoriaResponseDTO response =
                categoriaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Categoría creada correctamente",
                                response
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(
            summary = "Consultar categoría",
            description = "Obtiene una categoría por su identificador"
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {

        CategoriaResponseDTO response =
                categoriaService.buscarPorId(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categoría encontrada",
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(
            summary = "Listar categorías",
            description = "Obtiene las categorías con paginación y ordenamiento"
    )
    public ResponseEntity<ApiResponse<PageResponseDTO<CategoriaResponseDTO>>> listar(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {

        PageResponseDTO<CategoriaResponseDTO> response =
                categoriaService.listar(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categorías consultadas correctamente",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza el nombre y la descripción de una categoría"
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarCategoriaRequestDTO request
    ) {

        CategoriaResponseDTO response =
                categoriaService.actualizar(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categoría actualizada correctamente",
                        response
                )
        );
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(
            summary = "Activar categoría",
            description = "Activa una categoría previamente inactiva"
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> activar(
            @PathVariable Long id
    ) {

        CategoriaResponseDTO response =
                categoriaService.cambiarEstado(id, true);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categoría activada correctamente",
                        response
                )
        );
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(
            summary = "Desactivar categoría",
            description = "Desactiva una categoría sin eliminarla"
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> desactivar(
            @PathVariable Long id
    ) {

        CategoriaResponseDTO response =
                categoriaService.cambiarEstado(id, false);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categoría desactivada correctamente",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina definitivamente una categoría si no tiene registros asociados"
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id
    ) {

        categoriaService.eliminar(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categoría eliminada correctamente"
                )
        );
    }
}