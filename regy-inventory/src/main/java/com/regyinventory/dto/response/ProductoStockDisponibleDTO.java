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
public class ProductoStockDisponibleDTO {
    private Long productoId;
    private String numero;
    private String nombre;
    private String codigoBarras;
    private String sku;
    private Integer stockDisponible;
}
