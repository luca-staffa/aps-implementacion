package com.violeta.fia.dto;

import com.violeta.fia.domain.Piloto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PilotoRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotNull(message = "La fecha de nacimiento es obligatoria") @Past(message = "La fecha de nacimiento debe estar en el pasado") LocalDate fechaNacimiento,
        @NotBlank(message = "La nacionalidad es obligatoria") String nacionalidad,
        @NotNull(message = "El numero es obligatorio") @Positive(message = "El numero debe ser positivo") Integer numero,
        @NotBlank(message = "La abreviatura es obligatoria") @Pattern(regexp = "[A-Z]{3}", message = "La abreviatura debe tener exactamente 3 letras mayusculas") String abreviatura,
        @NotNull(message = "El rol es obligatorio") Piloto.RolPiloto rol,
        @NotBlank(message = "El numero de licencia FIA es obligatorio") String numeroLicenciaFia,
        String fotoUrl,
        @NotNull(message = "La categoria es obligatoria") Long categoriaId,
        @NotNull(message = "La temporada es obligatoria") Long temporadaId,
        Long escuderiaId
) {
}
