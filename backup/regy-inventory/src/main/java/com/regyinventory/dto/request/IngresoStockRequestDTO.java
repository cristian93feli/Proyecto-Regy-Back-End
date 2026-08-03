package com.regyinventory.dto.request;

import com.regyinventory.enums.TipoDestino;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class IngresoStockRequestDTO {
    @NotNull
    private Long productoId;
    @NotNull
    @Positive
    private Integer cantidad;
    @NotNull
    private TipoDestino tipoDestino;
    @NotNull
    private Long destinoId;
    private LocalDate fechaVencimiento;
    @Size(max = 500)
    private String observaciones;
}
