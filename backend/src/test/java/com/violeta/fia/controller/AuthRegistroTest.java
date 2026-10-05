package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.dto.RegistroRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRegistroTest extends BaseIntegrationTest {

    @Test
    void registrarDevuelve201ConRolPublico() throws Exception {
        String sufijo = Long.toString(System.nanoTime());
        RegistroRequest request = new RegistroRequest(
                "Nuevo", "Usuario", "nuevo." + sufijo + "@fia.com", "nuevo." + sufijo, "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nuevo." + sufijo))
                .andExpect(jsonPath("$.rol").value("PUBLICO"));
    }

    @Test
    void registrarUsernameDuplicadoDevuelve400() throws Exception {
        String sufijo = Long.toString(System.nanoTime());
        String username = "dup." + sufijo;

        RegistroRequest original = new RegistroRequest(
                "Primero", "Usuario", "primero." + sufijo + "@fia.com", username, "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(original)))
                .andExpect(status().isCreated());

        RegistroRequest duplicado = new RegistroRequest(
                "Segundo", "Usuario", "segundo." + sufijo + "@fia.com", username, "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarEmailDuplicadoDevuelve400() throws Exception {
        String sufijo = Long.toString(System.nanoTime());
        String email = "email." + sufijo + "@fia.com";

        RegistroRequest original = new RegistroRequest(
                "Primero", "Usuario", email, "uno." + sufijo, "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(original)))
                .andExpect(status().isCreated());

        RegistroRequest duplicado = new RegistroRequest(
                "Segundo", "Usuario", email, "dos." + sufijo, "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarContrasenaCortaDevuelve400() throws Exception {
        String sufijo = Long.toString(System.nanoTime());
        RegistroRequest request = new RegistroRequest(
                "Corto", "Usuario", "corto." + sufijo + "@fia.com", "corto." + sufijo, "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
