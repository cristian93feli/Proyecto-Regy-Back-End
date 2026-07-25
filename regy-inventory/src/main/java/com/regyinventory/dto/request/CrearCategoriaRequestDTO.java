package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import com.regyinventory.utils.constants.numeros.Numeros;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearCategoriaRequestDTO {

    @NotBlank(
            message = MensajesValidacion.Categoria.NOMBRE_OBLIGATORIO
    )
    @Size(
            max = Numeros.CIEN,
            message = MensajesValidacion.Categoria.NOMBRE_MAXIMO
    )
    private String nombre;

    @Size(
            max = Numeros.DOSCIENTOS_CINCUENTA,
            message = MensajesValidacion.Comun.DESCRIPCION_MAXIMA
    )
    private String descripcion;
}