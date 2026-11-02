package com.violeta.fia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EscuderiaRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 150, message = "El nombre no puede superar los 150 caracteres") String nombre,
        @Size(max = 100, message = "El pais no puede superar los 100 caracteres") String pais
) {
}
