package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarCategoriaRequestDTO;
import com.regyinventory.dto.request.CrearCategoriaRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.CategoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.service.contracts.ICategoriaService;
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

@RestController
@RequestMapping(RutasApi.CATEGORIAS)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Categoria.TAG,
        description = DocumentacionApi.Categoria.DESCRIPCION_TAG
)
@SecurityRequirement(
        name = DocumentacionApi.ESQUEMA_SEGURIDAD
)
public class CategoriaController {

    private final ICategoriaService categoriaService;

    @PostMapping
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_CREAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.CREAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_CREAR
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> crear(
            @Valid @RequestBody CrearCategoriaRequestDTO request
    ) {

        CategoriaResponseDTO response =
                categoriaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                MensajesExito.Categoria.CREADA,
                                response
                        )
                );
    }

    @GetMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.CONSULTAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_CONSULTAR
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {

        CategoriaResponseDTO response =
                categoriaService.buscarPorId(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Categoria.ENCONTRADA,
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.LISTAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_LISTAR
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
                        MensajesExito.Categoria.LISTADAS,
                        response
                )
        );
    }

    @PutMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.ACTUALIZAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_ACTUALIZAR
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarCategoriaRequestDTO request
    ) {

        CategoriaResponseDTO response =
                categoriaService.actualizar(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Categoria.ACTUALIZADA,
                        response
                )
        );
    }

    @PatchMapping(RutasApi.ACTIVAR)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.ACTIVAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_ACTIVAR
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> activar(
            @PathVariable Long id
    ) {

        CategoriaResponseDTO response =
                categoriaService.cambiarEstado(id, true);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Categoria.ACTIVADA,
                        response
                )
        );
    }

    @PatchMapping(RutasApi.DESACTIVAR)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.DESACTIVAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_DESACTIVAR
    )
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> desactivar(
            @PathVariable Long id
    ) {

        CategoriaResponseDTO response =
                categoriaService.cambiarEstado(id, false);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Categoria.DESACTIVADA,
                        response
                )
        );
    }

    @DeleteMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ELIMINAR
    )
    @Operation(
            summary = DocumentacionApi.Categoria.ELIMINAR,
            description = DocumentacionApi.Categoria.DESCRIPCION_ELIMINAR
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id
    ) {

        categoriaService.eliminar(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Categoria.ELIMINADA
                )
        );
    }
}