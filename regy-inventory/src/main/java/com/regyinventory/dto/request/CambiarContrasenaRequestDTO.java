package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import com.regyinventory.utils.constants.numeros.Numeros;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarContrasenaRequestDTO {

    @NotBlank(
            message = MensajesValidacion.Usuario.NUEVA_CONTRASENA_OBLIGATORIA
    )
    @Size(
            min = Numeros.OCHO,
            max = Numeros.CIEN,
            message = MensajesValidacion.Usuario.CONTRASENA_LONGITUD
    )
    private String newPassword;
}