package com.regyinventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos editables de una ubicación. El tipo y la jerarquía no se modifican.")
public class ActualizarUbicacionRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 80, message = "El código no puede superar 80 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripción no puede superar 250 caracteres")
    private String descripcion;

    private java.util.Set<Long> usuariosResponsablesIds;
}
