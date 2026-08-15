package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.numeros.Numeros;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = DocumentacionApi.Inventario.DESCRIPCION_VENTA_REQUEST)
public class VentaProductoRequestDTO {

    @NotNull
    private Long productoId;

    @NotNull
    private Long ubicacionOrigenId;

    @NotNull
    @Positive
    private Integer cantidad;

    @Size(max = Numeros.QUINIENTOS)
    private String observaciones;
}
