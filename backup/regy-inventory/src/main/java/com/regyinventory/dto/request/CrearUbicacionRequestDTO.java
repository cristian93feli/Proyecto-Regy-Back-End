package com.regyinventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class CrearUbicacionRequestDTO {
    @NotBlank
    @Size(max = 80)
    private String codigo;
    @NotBlank
    @Size(max = 120)
    private String nombre;
    @Size(max = 250)
    private String descripcion;
    @NotNull
    private Long depositoId;
}
