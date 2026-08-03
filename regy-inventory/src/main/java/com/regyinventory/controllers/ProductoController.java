package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarProductoRequestDTO;
import com.regyinventory.dto.request.CrearProductoRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.ProductoResponseDTO;
import com.regyinventory.service.contracts.IProductoService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.security.ExpresionesSeguridad;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.PRODUCTOS)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Producto.TAG,
        description = DocumentacionApi.Producto.DESCRIPCION_TAG
)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class ProductoController {

    private final IProductoService productoService;

    @PostMapping
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_CREAR)
    @Operation(
            summary = DocumentacionApi.Producto.CREAR,
            description = DocumentacionApi.Producto.DESCRIPCION_CREAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = DocumentacionApi.Respuesta.CREADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = DocumentacionApi.Respuesta.SOLICITUD_INVALIDA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = DocumentacionApi.Respuesta.SIN_PERMISO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = DocumentacionApi.Respuesta.CONFLICTO_NEGOCIO)
    })
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> crear(
            @Valid @RequestBody CrearProductoRequestDTO solicitudCreacion
    ) {
        ProductoResponseDTO productoCreado = productoService.crear(solicitudCreacion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MensajesExito.Producto.CREADO, productoCreado));
    }

    @GetMapping(RutasApi.POR_ID)
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_CONSULTAR)
    @Operation(
            summary = DocumentacionApi.Producto.CONSULTAR,
            description = DocumentacionApi.Producto.DESCRIPCION_CONSULTAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = DocumentacionApi.Respuesta.CONSULTA_EXITOSA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = DocumentacionApi.Respuesta.SIN_PERMISO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = DocumentacionApi.Respuesta.NO_ENCONTRADO)
    })
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> buscarPorId(
            @PathVariable Long id
    ) {
        ProductoResponseDTO productoEncontrado = productoService.buscarPorId(id);

        return ResponseEntity.ok(
                ApiResponse.success(MensajesExito.Producto.ENCONTRADO, productoEncontrado)
        );
    }

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_CONSULTAR)
    @Operation(
            summary = DocumentacionApi.Producto.LISTAR,
            description = DocumentacionApi.Producto.DESCRIPCION_LISTAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = DocumentacionApi.Respuesta.CONSULTA_EXITOSA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = DocumentacionApi.Respuesta.SIN_PERMISO)
    })
    public ResponseEntity<ApiResponse<PageResponseDTO<ProductoResponseDTO>>> listar(
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "10") Integer tamanoPagina,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "ASC") String direccionOrdenamiento
    ) {
        PageResponseDTO<ProductoResponseDTO> productos = productoService.listar(
                pagina,
                tamanoPagina,
                ordenarPor,
                direccionOrdenamiento
        );

        return ResponseEntity.ok(
                ApiResponse.success(MensajesExito.Producto.LISTADOS, productos)
        );
    }

    @PutMapping(RutasApi.POR_ID)
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_ACTUALIZAR)
    @Operation(
            summary = DocumentacionApi.Producto.ACTUALIZAR,
            description = DocumentacionApi.Producto.DESCRIPCION_ACTUALIZAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = DocumentacionApi.Respuesta.ACTUALIZADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = DocumentacionApi.Respuesta.SOLICITUD_INVALIDA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = DocumentacionApi.Respuesta.SIN_PERMISO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = DocumentacionApi.Respuesta.NO_ENCONTRADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = DocumentacionApi.Respuesta.CONFLICTO_NEGOCIO)
    })
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarProductoRequestDTO solicitudActualizacion
    ) {
        ProductoResponseDTO productoActualizado = productoService.actualizar(id, solicitudActualizacion);

        return ResponseEntity.ok(
                ApiResponse.success(MensajesExito.Producto.ACTUALIZADO, productoActualizado)
        );
    }

    @PatchMapping(RutasApi.ACTIVAR)
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_ACTUALIZAR)
    @Operation(
            summary = DocumentacionApi.Producto.ACTIVAR,
            description = DocumentacionApi.Producto.DESCRIPCION_ACTIVAR
    )
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> activar(
            @PathVariable Long id
    ) {
        ProductoResponseDTO productoActivado = productoService.cambiarEstado(id, true);

        return ResponseEntity.ok(
                ApiResponse.success(MensajesExito.Producto.ACTIVADO, productoActivado)
        );
    }

    @PatchMapping(RutasApi.DESACTIVAR)
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_ACTUALIZAR)
    @Operation(
            summary = DocumentacionApi.Producto.DESACTIVAR,
            description = DocumentacionApi.Producto.DESCRIPCION_DESACTIVAR
    )
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> desactivar(
            @PathVariable Long id
    ) {
        ProductoResponseDTO productoDesactivado = productoService.cambiarEstado(id, false);

        return ResponseEntity.ok(
                ApiResponse.success(MensajesExito.Producto.DESACTIVADO, productoDesactivado)
        );
    }

    @DeleteMapping(RutasApi.POR_ID)
    @PreAuthorize(ExpresionesSeguridad.PRODUCTO_ELIMINAR)
    @Operation(
            summary = DocumentacionApi.Producto.ELIMINAR,
            description = DocumentacionApi.Producto.DESCRIPCION_ELIMINAR
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id
    ) {
        productoService.eliminar(id);

        return ResponseEntity.ok(ApiResponse.success(MensajesExito.Producto.ELIMINADO));
    }
}
