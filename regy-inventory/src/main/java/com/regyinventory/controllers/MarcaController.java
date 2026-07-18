package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarMarcaRequestDTO;
import com.regyinventory.dto.request.CrearMarcaRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.MarcaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.service.contracts.IMarcaService;
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
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
@Tag(
        name = "Marcas",
        description = "Administración de marcas"
)
@SecurityRequirement(name = "bearerAuth")
public class MarcaController {

    private final IMarcaService marcaService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @Operation(summary = "Crear marca")
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> crear(
            @Valid @RequestBody CrearMarcaRequestDTO request
    ) {

        MarcaResponseDTO response =
                marcaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Marca creada correctamente",
                                response
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Consultar marca")
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marca encontrada",
                        marcaService.buscarPorId(id)
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Listar marcas")
    public ResponseEntity<ApiResponse<PageResponseDTO<MarcaResponseDTO>>> listar(

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "ASC")
            String direction
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marcas consultadas correctamente",
                        marcaService.listar(
                                page,
                                size,
                                sortBy,
                                direction
                        )
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Actualizar marca")
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> actualizar(

            @PathVariable Long id,

            @Valid
            @RequestBody
            ActualizarMarcaRequestDTO request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marca actualizada correctamente",
                        marcaService.actualizar(
                                id,
                                request
                        )
                )
        );
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Activar marca")
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> activar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marca activada correctamente",
                        marcaService.cambiarEstado(
                                id,
                                true
                        )
                )
        );
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Desactivar marca")
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> desactivar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marca desactivada correctamente",
                        marcaService.cambiarEstado(
                                id,
                                false
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @Operation(
            summary = "Eliminar marca",
            description = "Elimina definitivamente una marca si no tiene registros asociados"
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id
    ) {

        marcaService.eliminar(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marca eliminada correctamente"
                )
        );
    }

}