package com.regyinventory.dto.response;

import com.regyinventory.enums.UnidadMedida;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa de un producto")
public class ProductoResponseDTO {

    @Schema(description = "Identificador del producto", example = "1")
    private Long id;

    @Schema(description = DocumentacionApi.Producto.NUMERO, example = "PROD-0001")
    private String numero;

    @Schema(description = DocumentacionApi.Producto.NOMBRE, example = "Tomate chonto")
    private String nombre;

    @Schema(description = DocumentacionApi.Producto.MARCA_ID, example = "1")
    private Long marcaId;

    @Schema(description = "Nombre de la marca", example = "Marca agrícola")
    private String marcaNombre;

    @Schema(description = DocumentacionApi.Producto.CATEGORIA_ID, example = "1")
    private Long categoriaId;

    @Schema(description = "Nombre de la categoría", example = "Verduras")
    private String categoriaNombre;

    @Schema(description = DocumentacionApi.Producto.SKU, example = "TOM-CH-001")
    private String sku;

    @Schema(description = DocumentacionApi.Producto.CODIGO_BARRAS, example = "7701234567890")
    private String codigoBarras;

    @Schema(description = DocumentacionApi.Producto.IMAGEN_URL)
    private String imagenUrl;

    @Schema(description = DocumentacionApi.Producto.PRECIO_COMPRA, example = "1500.00")
    private BigDecimal precioCompra;

    @Schema(description = DocumentacionApi.Producto.PRECIO_VENTA, example = "2200.00")
    private BigDecimal precioVenta;

    @Schema(description = DocumentacionApi.Producto.UNIDAD_MEDIDA, example = "UNIDAD")
    private UnidadMedida unidadMedida;

    @Schema(description = DocumentacionApi.Producto.STOCK_MINIMO, example = "10")
    private Integer stockMinimo;

    @Schema(description = DocumentacionApi.Producto.STOCK_TOTAL, example = "125")
    private Integer stockTotal;

    @Schema(description = DocumentacionApi.Producto.ACTIVO, example = "true")
    private Boolean activo;
}
