package com.regyinventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserCreateRequestDTO {

    @NotBlank(message = "La identificación es obligatoria")
    @Size(max = 20, message = "La identificación no puede superar 20 caracteres")
    private String identificacion;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 80, message = "El apellido no puede superar 80 caracteres")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 40, message = "El usuario debe tener entre 4 y 40 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;

    @NotEmpty(message = "Debes asignar al menos un rol")
    private Set<Long> roleIds;
}