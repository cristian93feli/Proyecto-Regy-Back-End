package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String numero;
    private String nombre;
    private Long marcaId;
    private String marcaNombre;
    private Long categoriaId;
    private String categoriaNombre;
    private String sku;
    private String codigoBarras;
    private String imagenUrl;
    private java.math.BigDecimal precioCompra;
    private java.math.BigDecimal precioVenta;
    private com.regyinventory.enums.UnidadMedida unidadMedida;
    private Integer stockMinimo;
    private Integer stockTotal;
    private Boolean activo;
}
