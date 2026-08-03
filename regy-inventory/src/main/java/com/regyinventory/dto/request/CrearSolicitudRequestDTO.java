package com.regyinventory.dto.request;

import com.regyinventory.enums.PrioridadSolicitud;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class CrearSolicitudRequestDTO {
    @NotNull
    private Long productoId;
    @NotNull
    @Positive
    private Integer cantidad;
    @NotNull
    private PrioridadSolicitud prioridad;
    @NotNull
    private Long zonaDestinoId;
    @Size(max = 500)
    private String observaciones;
}
