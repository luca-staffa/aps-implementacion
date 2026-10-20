package com.violeta.fia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ActualizarDatosRequest(
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") String email,
        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "El telefono no es valido")
        String telefono
) {
}
