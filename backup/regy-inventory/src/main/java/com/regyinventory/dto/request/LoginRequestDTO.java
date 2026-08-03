package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(
            message = MensajesValidacion.Login.USUARIO_OBLIGATORIO
    )
    private String username;

    @NotBlank(
            message = MensajesValidacion.Login.CONTRASENA_OBLIGATORIA
    )
    private String password;
}