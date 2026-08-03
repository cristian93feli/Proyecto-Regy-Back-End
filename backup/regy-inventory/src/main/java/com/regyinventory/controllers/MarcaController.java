package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarMarcaRequestDTO;
import com.regyinventory.dto.request.CrearMarcaRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.MarcaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.service.contracts.IMarcaService;
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
@RequestMapping(RutasApi.MARCAS)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Marca.TAG,
        description = DocumentacionApi.Marca.DESCRIPCION_TAG
)
@SecurityRequirement(
        name = DocumentacionApi.ESQUEMA_SEGURIDAD
)
public class MarcaController {

    private final IMarcaService marcaService;

    @PostMapping
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_CREAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.CREAR,
            description = DocumentacionApi.Marca.DESCRIPCION_CREAR
    )
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> crear(
            @Valid @RequestBody CrearMarcaRequestDTO request
    ) {

        MarcaResponseDTO response =
                marcaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                MensajesExito.Marca.CREADA,
                                response
                        )
                );
    }

    @GetMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.CONSULTAR,
            description = DocumentacionApi.Marca.DESCRIPCION_CONSULTAR
    )
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {

        MarcaResponseDTO response =
                marcaService.buscarPorId(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Marca.ENCONTRADA,
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_CONSULTAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.LISTAR,
            description = DocumentacionApi.Marca.DESCRIPCION_LISTAR
    )
    public ResponseEntity<ApiResponse<PageResponseDTO<MarcaResponseDTO>>> listar(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {

        PageResponseDTO<MarcaResponseDTO> response =
                marcaService.listar(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Marca.LISTADAS,
                        response
                )
        );
    }

    @PutMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.ACTUALIZAR,
            description = DocumentacionApi.Marca.DESCRIPCION_ACTUALIZAR
    )
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarMarcaRequestDTO request
    ) {

        MarcaResponseDTO response =
                marcaService.actualizar(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Marca.ACTUALIZADA,
                        response
                )
        );
    }

    @PatchMapping(RutasApi.ACTIVAR)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.ACTIVAR,
            description = DocumentacionApi.Marca.DESCRIPCION_ACTIVAR
    )
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> activar(
            @PathVariable Long id
    ) {

        MarcaResponseDTO response =
                marcaService.cambiarEstado(id, true);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Marca.ACTIVADA,
                        response
                )
        );
    }

    @PatchMapping(RutasApi.DESACTIVAR)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ACTUALIZAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.DESACTIVAR,
            description = DocumentacionApi.Marca.DESCRIPCION_DESACTIVAR
    )
    public ResponseEntity<ApiResponse<MarcaResponseDTO>> desactivar(
            @PathVariable Long id
    ) {

        MarcaResponseDTO response =
                marcaService.cambiarEstado(id, false);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Marca.DESACTIVADA,
                        response
                )
        );
    }

    @DeleteMapping(RutasApi.POR_ID)
    @PreAuthorize(
            ExpresionesSeguridad.PRODUCTO_ELIMINAR
    )
    @Operation(
            summary = DocumentacionApi.Marca.ELIMINAR,
            description = DocumentacionApi.Marca.DESCRIPCION_ELIMINAR
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id
    ) {

        marcaService.eliminar(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        MensajesExito.Marca.ELIMINADA
                )
        );
    }
}