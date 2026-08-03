package com.regyinventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para trasladar inventario entre cajas y zonas de empaque")
public class MoverStockRequestDTO {
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
    @NotNull(message = "La ubicación de origen es obligatoria")
    private Long ubicacionOrigenId;
    @NotNull(message = "La ubicación de destino es obligatoria")
    private Long ubicacionDestinoId;
    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observaciones;
}
