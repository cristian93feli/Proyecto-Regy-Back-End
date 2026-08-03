package com.regyinventory.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class CompletarSolicitudRequestDTO {
    @Size(max = 500)
    private String observaciones;
}
