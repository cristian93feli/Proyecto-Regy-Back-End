package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarCategoriaRequestDTO {

    @NotBlank(
            message = MensajesValidacion.Categoria.NOMBRE_OBLIGATORIO
    )
    @Size(
            max = 100,
            message = MensajesValidacion.Categoria.NOMBRE_MAXIMO
    )
    private String nombre;

    @Size(
            max = 250,
            message = MensajesValidacion.Comun.DESCRIPCION_MAXIMA
    )
    private String descripcion;
}