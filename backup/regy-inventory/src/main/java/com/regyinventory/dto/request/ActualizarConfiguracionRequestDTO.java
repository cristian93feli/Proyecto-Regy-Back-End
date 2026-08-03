package com.regyinventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class ActualizarConfiguracionRequestDTO {
    @NotBlank
    @Size(max = 500)
    private String valor;
}
