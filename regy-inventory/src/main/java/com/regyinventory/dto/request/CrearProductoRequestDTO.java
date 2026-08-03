package com.regyinventory.dto.request;

import com.regyinventory.enums.UnidadMedida;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Datos requeridos para registrar un producto")
public class CrearProductoRequestDTO {

    @NotBlank
    @Size(max = 50)
    @Schema(description = DocumentacionApi.Producto.NUMERO, example = "PROD-0001")
    private String numero;

    @NotBlank
    @Size(max = 150)
    @Schema(description = DocumentacionApi.Producto.NOMBRE, example = "Tomate chonto")
    private String nombre;

    @Schema(description = DocumentacionApi.Producto.MARCA_ID, example = "1")
    private Long marcaId;

    @Schema(description = DocumentacionApi.Producto.CATEGORIA_ID, example = "1")
    private Long categoriaId;

    @Size(max = 80)
    @Schema(description = DocumentacionApi.Producto.SKU, example = "TOM-CH-001")
    private String sku;

    @Size(max = 100)
    @Schema(description = DocumentacionApi.Producto.CODIGO_BARRAS, example = "7701234567890")
    private String codigoBarras;

    @Size(max = 500)
    @Schema(
            description = DocumentacionApi.Producto.IMAGEN_URL,
            example = "https://cdn.example.com/productos/tomate.png"
    )
    private String imagenUrl;

    @DecimalMin("0.0")
    @Schema(description = DocumentacionApi.Producto.PRECIO_COMPRA, example = "1500.00")
    private BigDecimal precioCompra;

    @DecimalMin("0.0")
    @Schema(description = DocumentacionApi.Producto.PRECIO_VENTA, example = "2200.00")
    private BigDecimal precioVenta;

    @NotNull
    @Schema(description = DocumentacionApi.Producto.UNIDAD_MEDIDA, example = "UNIDAD")
    private UnidadMedida unidadMedida;

    @NotNull
    @PositiveOrZero
    @Schema(description = DocumentacionApi.Producto.STOCK_MINIMO, example = "10")
    private Integer stockMinimo;
}
