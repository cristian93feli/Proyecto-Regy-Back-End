package com.regyinventory.dto.request;

import com.regyinventory.enums.TipoDestino;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class MoverStockRequestDTO {
    @NotNull
    private Long productoId;
    @NotNull
    @Positive
    private Integer cantidad;
    @NotNull
    private TipoDestino tipoOrigen;
    @NotNull
    private Long origenId;
    @NotNull
    private TipoDestino tipoDestino;
    @NotNull
    private Long destinoId;
    @Size(max = 500)
    private String observaciones;
}
