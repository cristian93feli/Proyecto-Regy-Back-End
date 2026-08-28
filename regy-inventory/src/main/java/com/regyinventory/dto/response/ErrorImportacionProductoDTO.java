package com.regyinventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorImportacionProductoDTO {
    private Integer fila;
    private String numeroProducto;
    private String marca;
    private String categoria;
    private String codigoBarras;
    private String nombreProducto;
    private String sku;
    private String mensaje;
}
