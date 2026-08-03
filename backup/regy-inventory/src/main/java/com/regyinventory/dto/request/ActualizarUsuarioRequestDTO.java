package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import com.regyinventory.utils.constants.numeros.Numeros;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ActualizarUsuarioRequestDTO {

    @NotBlank(
            message = MensajesValidacion.Usuario.IDENTIFICACION_OBLIGATORIA
    )
    @Size(
            max = Numeros.VEINTE,
            message = MensajesValidacion.Usuario.IDENTIFICACION_MAXIMA
    )
    private String identificacion;

    @NotBlank(
            message = MensajesValidacion.Usuario.NOMBRE_OBLIGATORIO
    )
    @Size(
            max = Numeros.OCHENTA,
            message = MensajesValidacion.Usuario.NOMBRE_MAXIMO
    )
    private String nombre;

    @NotBlank(
            message = MensajesValidacion.Usuario.APELLIDO_OBLIGATORIO
    )
    @Size(
            max = Numeros.OCHENTA,
            message = MensajesValidacion.Usuario.APELLIDO_MAXIMO
    )
    private String apellido;

    @NotBlank(
            message = MensajesValidacion.Usuario.CORREO_OBLIGATORIO
    )
    @Email(
            message = MensajesValidacion.Usuario.CORREO_INVALIDO
    )
    private String correo;

    @NotBlank(
            message = MensajesValidacion.Usuario.USERNAME_OBLIGATORIO
    )
    @Size(
            min = Numeros.CUATRO,
            max = Numeros.CUARENTA,
            message = MensajesValidacion.Usuario.USERNAME_LONGITUD
    )
    private String username;

    @NotEmpty(
            message = MensajesValidacion.Usuario.ROL_OBLIGATORIO
    )
    private Set<Long> roleIds;
}