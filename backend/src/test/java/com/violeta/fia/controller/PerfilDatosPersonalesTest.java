package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PerfilDatosPersonalesTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void restaurarDatos() {
        Usuario publico = usuarioRepository.findByUsername("publico").orElseThrow();
        publico.setEmail("publico@fia.com");
        publico.setTelefono(null);
        usuarioRepository.save(publico);
    }

    @Test
    void actualizarDatosPropiosDevuelve200() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        String cuerpo = "{\"email\":\"publico.nuevo@fia.com\",\"telefono\":\"+54 11 5555-1234\"}";
        mockMvc.perform(put("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("publico.nuevo@fia.com"))
                .andExpect(jsonPath("$.telefono").value("+54 11 5555-1234"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("publico.nuevo@fia.com"))
                .andExpect(jsonPath("$.telefono").value("+54 11 5555-1234"));
    }

    @Test
    void actualizarSinTokenDevuelve401() throws Exception {
        String cuerpo = "{\"email\":\"publico.nuevo@fia.com\",\"telefono\":\"+54 11 5555-1234\"}";

        mockMvc.perform(put("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void emailInvalidoDevuelve400() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        String cuerpo = "{\"email\":\"correo-invalido\",\"telefono\":\"+54 11 5555-1234\"}";
        mockMvc.perform(put("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("email")));
    }

    @Test
    void telefonoInvalidoDevuelve400() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        String cuerpo = "{\"email\":\"publico.nuevo@fia.com\",\"telefono\":\"abc\"}";
        mockMvc.perform(put("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("telefono")));
    }

    @Test
    void emailDeOtroUsuarioDevuelve400() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        String cuerpo = "{\"email\":\"admin@fia.com\",\"telefono\":\"+54 11 5555-1234\"}";
        mockMvc.perform(put("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("email")));
    }
}
