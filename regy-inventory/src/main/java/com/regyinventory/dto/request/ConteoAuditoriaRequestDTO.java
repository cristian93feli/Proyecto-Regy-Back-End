package com.regyinventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class ConteoAuditoriaRequestDTO {
    @NotNull
    private Long productoId;
    @NotNull
    @PositiveOrZero
    private Integer cantidadContada;
}
