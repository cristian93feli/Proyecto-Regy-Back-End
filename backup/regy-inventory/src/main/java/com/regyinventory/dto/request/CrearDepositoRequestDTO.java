package com.regyinventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class CrearDepositoRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String nombre;
    @Size(max = 250)
    private String descripcion;
}
