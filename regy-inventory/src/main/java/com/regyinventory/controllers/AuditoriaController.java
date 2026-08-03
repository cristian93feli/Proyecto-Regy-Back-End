package com.regyinventory.controllers;

import com.regyinventory.dto.request.CrearAuditoriaRequestDTO;
import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.AuditoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.service.contracts.IAuditoriaService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RutasApi.AUDITORIAS)
@RequiredArgsConstructor
@Tag(name = DocumentacionApi.Auditoria.TAG, description = DocumentacionApi.Auditoria.DESCRIPCION_TAG)
@SecurityRequirement(name = DocumentacionApi.ESQUEMA_SEGURIDAD)
public class AuditoriaController {

    private final IAuditoriaService auditoriaService;

    @PostMapping
    @PreAuthorize(ExpresionesSeguridad.AUDITORIA_GESTIONAR)
    @Operation(summary = DocumentacionApi.Auditoria.CREAR)
    public ResponseEntity<ApiResponse<AuditoriaResponseDTO>> crear(
            @Valid @RequestBody CrearAuditoriaRequestDTO solicitudAuditoria
    ) {
        AuditoriaResponseDTO auditoriaRegistrada = auditoriaService.crear(solicitudAuditoria);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MensajesExito.Auditoria.REGISTRADA, auditoriaRegistrada));
    }

    @GetMapping
    @PreAuthorize(ExpresionesSeguridad.AUDITORIA_GESTIONAR)
    @Operation(summary = DocumentacionApi.Auditoria.LISTAR)
    public ApiResponse<PageResponseDTO<AuditoriaResponseDTO>> listar(
            @RequestParam(defaultValue = ValoresApi.PAGINA_INICIAL) Integer numeroPagina,
            @RequestParam(defaultValue = ValoresApi.TAMANO_PAGINA) Integer tamanoPagina
    ) {
        return ApiResponse.success(
                MensajesExito.Auditoria.LISTADAS,
                auditoriaService.listar(numeroPagina, tamanoPagina)
        );
    }
}
