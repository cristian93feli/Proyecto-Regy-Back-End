package com.regyinventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para ingresar stock en una caja")
public class IngresoStockRequestDTO {
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
    @NotNull(message = "La caja de destino es obligatoria")
    @Schema(description = "Debe corresponder a una ubicación activa de tipo CAJA")
    private Long ubicacionDestinoId;
    private LocalDate fechaVencimiento;
    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observaciones;
}
