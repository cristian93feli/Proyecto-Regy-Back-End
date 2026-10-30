package com.regyinventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fila rechazada y motivo funcional para consulta y reporte de Excel. */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorImportacionStockDTO {
    private Integer fila;
    private String numeroProducto;
    private String sku;
    private String codigoBarras;
    private String codigoUbicacion;
    private String cantidad;
    private String fechaVencimiento;
    private String observaciones;
    private String mensaje;
}
