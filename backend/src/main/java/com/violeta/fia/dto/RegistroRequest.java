package com.violeta.fia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") String email,
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contrasena es obligatoria") @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres") String password
) {
}
