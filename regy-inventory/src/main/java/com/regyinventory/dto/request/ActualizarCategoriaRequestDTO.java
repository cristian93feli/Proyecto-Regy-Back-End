package com.regyinventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarCategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(
            max = 100,
            message = "El nombre no puede superar 100 caracteres"
    )
    private String nombre;

    @Size(
            max = 250,
            message = "La descripción no puede superar 250 caracteres"
    )
    private String descripcion;
}