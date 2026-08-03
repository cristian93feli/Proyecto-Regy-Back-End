package com.regyinventory.dto.request;

import com.regyinventory.enums.UnidadMedida;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
public class CrearProductoRequestDTO {
    @NotBlank
    @Size(max = 50)
    private String numero;
    @NotBlank
    @Size(max = 150)
    private String nombre;
    private Long marcaId;
    private Long categoriaId;
    @Size(max = 80)
    private String sku;
    @Size(max = 100)
    private String codigoBarras;
    @Size(max = 500)
    private String imagenUrl;
    @DecimalMin("0.0" )
    private BigDecimal precioCompra;
    @DecimalMin("0.0" )
    private BigDecimal precioVenta;
    @NotNull
    private UnidadMedida unidadMedida;
    @NotNull
    @PositiveOrZero
    private Integer stockMinimo;
}
