package com.regyinventory.controllers;

import com.regyinventory.dto.response.ApiResponse;
import com.regyinventory.dto.response.ReporteTemporalDTO;
import com.regyinventory.service.implementation.reportes.ReportesTemporalesService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes-temporales")
@RequiredArgsConstructor
public class ReportesTemporalesController {
    private final ReportesTemporalesService reportes;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_CREATE','STOCK_RECEIVE')")
    public ApiResponse<List<ReporteTemporalDTO>> listar() {
        return ApiResponse.success("Reportes disponibles durante siete días", reportes.listar());
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAnyAuthority('PRODUCT_CREATE','STOCK_RECEIVE')")
    public ResponseEntity<byte[]> descargar(@PathVariable String id) {
        var archivo = reportes.descargar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.nombre() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(archivo.contenido());
    }
}
