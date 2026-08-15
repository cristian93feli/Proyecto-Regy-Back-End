package com.regyinventory.controllers;

import com.regyinventory.dto.request.CompletarSolicitudRequestDTO;
import com.regyinventory.dto.request.CrearSolicitudRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.SolicitudResponseDTO;
import com.regyinventory.dto.response.SugerenciaReposicionDTO;
import java.util.List;
import com.regyinventory.enums.EstadoSolicitud;
import com.regyinventory.service.contracts.ISolicitudReposicionService;
import com.regyinventory.utils.constants.api.RutasApi;
import com.regyinventory.utils.constants.api.ValoresApi;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.SOLICITUDES_REPOSICION)
@RequiredArgsConstructor
@Tag(name = DocumentacionApi.SolicitudReposicion.TAG, description = DocumentacionApi.SolicitudReposicion.DESCRIPCION_TAG)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class SolicitudReposicionController {

    private final ISolicitudReposicionService solicitudReposicionService;

    @PostMapping
    @PreAuthorize(ExpresionesSeguridad.SOLICITUD_CREAR)
    @Operation(summary = DocumentacionApi.SolicitudReposicion.CREAR)
    public ResponseEntity<ApiResponse<SolicitudResponseDTO>> crear(
            @Valid @RequestBody CrearSolicitudRequestDTO solicitudCreacion
    ) {
        SolicitudResponseDTO solicitudCreada = solicitudReposicionService.crear(solicitudCreacion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MensajesExito.SolicitudReposicion.CREADA, solicitudCreada));
    }

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.SOLICITUD_CONSULTAR)
    @Operation(summary = DocumentacionApi.SolicitudReposicion.LISTAR)
    public ApiResponse<PageResponseDTO<SolicitudResponseDTO>> listar(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(defaultValue = ValoresApi.PAGINA_INICIAL) Integer numeroPagina,
            @RequestParam(defaultValue = ValoresApi.TAMANO_PAGINA) Integer tamanoPagina
    ) {
        return ApiResponse.success(
                MensajesExito.SolicitudReposicion.LISTADAS,
                solicitudReposicionService.listar(estado, numeroPagina, tamanoPagina)
        );
    }

    @GetMapping(RutasApi.SUGERENCIAS_SOLICITUD)
    @PreAuthorize(ExpresionesSeguridad.SOLICITUD_CONSULTAR)
    @Operation(summary = DocumentacionApi.SolicitudReposicion.SUGERENCIAS)
    public ApiResponse<List<SugerenciaReposicionDTO>> sugerencias(@PathVariable Long id) {
        return ApiResponse.success(
                MensajesExito.SolicitudReposicion.SUGERENCIAS,
                solicitudReposicionService.sugerencias(id)
        );
    }

    @PostMapping(RutasApi.COMPLETAR)
    @PreAuthorize(ExpresionesSeguridad.SOLICITUD_COMPLETAR)
    @Operation(summary = DocumentacionApi.SolicitudReposicion.COMPLETAR)
    public ApiResponse<SolicitudResponseDTO> completar(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CompletarSolicitudRequestDTO solicitudCompletado
    ) {
        SolicitudResponseDTO solicitudProcesada = solicitudReposicionService.completar(id, solicitudCompletado);
        String mensaje = solicitudProcesada.getEstado() == EstadoSolicitud.COMPLETADA
                ? MensajesExito.SolicitudReposicion.COMPLETADA
                : MensajesExito.SolicitudReposicion.COMPLETADA_PARCIAL;
        return ApiResponse.success(mensaje, solicitudProcesada);
    }

    @PatchMapping(RutasApi.CANCELAR)
    @PreAuthorize(ExpresionesSeguridad.SOLICITUD_CANCELAR)
    @Operation(summary = DocumentacionApi.SolicitudReposicion.CANCELAR)
    public ApiResponse<SolicitudResponseDTO> cancelar(@PathVariable Long id) {
        return ApiResponse.success(
                MensajesExito.SolicitudReposicion.CANCELADA,
                solicitudReposicionService.cancelar(id)
        );
    }
}
