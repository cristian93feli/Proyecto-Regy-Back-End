package com.regyinventory.controllers;

import com.regyinventory.dto.request.ActualizarUbicacionRequestDTO;
import com.regyinventory.dto.request.CrearUbicacionRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UbicacionResponseDTO;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.service.contracts.IUbicacionService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.api.ValoresApi;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.security.ExpresionesSeguridad;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping(RutasApi.UBICACIONES)
@RequiredArgsConstructor
@Tag(
        name = DocumentacionApi.Ubicacion.TAG,
        description = DocumentacionApi.Ubicacion.DESCRIPCION_TAG
)
@SecurityRequirement(name = DocumentacionApi.SEGURIDAD_BEARER)
public class UbicacionController {

    private final IUbicacionService ubicacionService;

    @PostMapping
    @PreAuthorize(ExpresionesSeguridad.UBICACION_CREAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.CREAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_CREAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.CREADO, description = DocumentacionApi.Respuesta.CREADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.SOLICITUD_INVALIDA, description = DocumentacionApi.Respuesta.SOLICITUD_INVALIDA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.NO_AUTENTICADO, description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.SIN_PERMISO, description = DocumentacionApi.Respuesta.SIN_PERMISO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.CONFLICTO, description = DocumentacionApi.Respuesta.CONFLICTO_NEGOCIO)
    })
    public ResponseEntity<ApiResponse<UbicacionResponseDTO>> crear(
            @Valid @RequestBody CrearUbicacionRequestDTO solicitudCreacion
    ) {
        UbicacionResponseDTO ubicacionCreada = ubicacionService.crear(solicitudCreacion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MensajesExito.Ubicacion.CREADA, ubicacionCreada));
    }

    @GetMapping(RutasApi.UBICACION_POR_ID)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_CONSULTAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.CONSULTAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_CONSULTAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.EXITO, description = DocumentacionApi.Respuesta.CONSULTA_EXITOSA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.NO_AUTENTICADO, description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.SIN_PERMISO, description = DocumentacionApi.Respuesta.SIN_PERMISO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.NO_ENCONTRADO, description = DocumentacionApi.Respuesta.NO_ENCONTRADO)
    })
    public ApiResponse<UbicacionResponseDTO> buscarPorId(@PathVariable Long ubicacionId) {
        return ApiResponse.success(
                MensajesExito.Ubicacion.ENCONTRADA,
                ubicacionService.buscar(ubicacionId)
        );
    }

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.UBICACION_CONSULTAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.LISTAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_LISTAR
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.EXITO, description = DocumentacionApi.Respuesta.CONSULTA_EXITOSA),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.NO_AUTENTICADO, description = DocumentacionApi.Respuesta.NO_AUTENTICADO),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = DocumentacionApi.CodigoRespuesta.SIN_PERMISO, description = DocumentacionApi.Respuesta.SIN_PERMISO)
    })
    public ApiResponse<PageResponseDTO<UbicacionResponseDTO>> listar(
            @RequestParam(defaultValue = ValoresApi.PAGINA_INICIAL) Integer numeroPagina,
            @RequestParam(defaultValue = ValoresApi.TAMANO_PAGINA) Integer tamanoPagina,
            @RequestParam(defaultValue = ValoresApi.ORDEN_POR_ID) String campoOrdenamiento,
            @RequestParam(defaultValue = ValoresApi.ORDEN_ASCENDENTE) String direccionOrdenamiento
    ) {
        PageResponseDTO<UbicacionResponseDTO> ubicaciones = ubicacionService.listar(
                numeroPagina,
                tamanoPagina,
                campoOrdenamiento,
                direccionOrdenamiento
        );

        return ApiResponse.success(MensajesExito.Ubicacion.LISTADAS, ubicaciones);
    }

    @GetMapping(RutasApi.POR_TIPO)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_CONSULTAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.LISTAR_TIPO,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_LISTAR_TIPO
    )
    public ApiResponse<List<UbicacionResponseDTO>> listarPorTipo(
            @PathVariable TipoUbicacion tipoUbicacion
    ) {
        return ApiResponse.success(
                MensajesExito.Ubicacion.LISTADAS,
                ubicacionService.listarPorTipo(tipoUbicacion)
        );
    }

    @GetMapping(RutasApi.UBICACIONES_HIJAS)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_CONSULTAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.LISTAR_HIJAS,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_LISTAR_HIJAS
    )
    public ApiResponse<List<UbicacionResponseDTO>> listarHijas(
            @PathVariable Long ubicacionPadreId
    ) {
        return ApiResponse.success(
                MensajesExito.Ubicacion.HIJAS_LISTADAS,
                ubicacionService.listarHijas(ubicacionPadreId)
        );
    }

    @PutMapping(RutasApi.UBICACION_POR_ID)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_ACTUALIZAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.ACTUALIZAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_ACTUALIZAR
    )
    public ApiResponse<UbicacionResponseDTO> actualizar(
            @PathVariable Long ubicacionId,
            @Valid @RequestBody ActualizarUbicacionRequestDTO solicitudActualizacion
    ) {
        return ApiResponse.success(
                MensajesExito.Ubicacion.ACTUALIZADA,
                ubicacionService.actualizar(ubicacionId, solicitudActualizacion)
        );
    }

    @PatchMapping(RutasApi.UBICACION_ACTIVAR)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_ACTUALIZAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.ACTIVAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_ACTIVAR
    )
    public ApiResponse<UbicacionResponseDTO> activar(@PathVariable Long ubicacionId) {
        return ApiResponse.success(
                MensajesExito.Ubicacion.ACTIVADA,
                ubicacionService.cambiarEstado(ubicacionId, true)
        );
    }

    @PatchMapping(RutasApi.UBICACION_DESACTIVAR)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_ACTUALIZAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.DESACTIVAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_DESACTIVAR
    )
    public ApiResponse<UbicacionResponseDTO> desactivar(@PathVariable Long ubicacionId) {
        return ApiResponse.success(
                MensajesExito.Ubicacion.DESACTIVADA,
                ubicacionService.cambiarEstado(ubicacionId, false)
        );
    }

    @DeleteMapping(RutasApi.UBICACION_POR_ID)
    @PreAuthorize(ExpresionesSeguridad.UBICACION_ELIMINAR)
    @Operation(
            summary = DocumentacionApi.Ubicacion.ELIMINAR,
            description = DocumentacionApi.Ubicacion.DESCRIPCION_ELIMINAR
    )
    public ApiResponse<Void> eliminar(@PathVariable Long ubicacionId) {
        ubicacionService.eliminar(ubicacionId);
        return ApiResponse.success(MensajesExito.Ubicacion.ELIMINADA);
    }
}
