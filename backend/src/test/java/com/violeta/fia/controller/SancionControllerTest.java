package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.dto.RegistroRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SancionControllerTest extends BaseIntegrationTest {

    private Long registrarPilotoDePrueba() throws Exception {
        String sufijo = Long.toString(System.nanoTime());
        String cuerpo = objectMapper.writeValueAsString(new RegistroRequest(
                "Piloto", "Prueba", "piloto." + sufijo + "@fia.com", "piloto." + sufijo, "Password123!"));
        String respuesta = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = objectMapper.readTree(respuesta);
        return nodo.get("id").asLong();
    }

    private String cuerpoSancion(Long pilotoId) {
        return "{\"pilotoId\":" + pilotoId + ",\"categoria\":\"F1\",\"motivo\":\"Prueba\","
                + "\"fecha\":\"2026-06-01\",\"tipo\":\"DEPORTIVA\",\"descripcion\":\"Prueba\"}";
    }

    @Test
    void altaPorAdminFiaDevuelve201() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        Long pilotoId = registrarPilotoDePrueba();

        mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSancion(pilotoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.motivo").value("Prueba"));
    }

    @Test
    void altaPorAdminEscuderiaDevuelve403() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");
        Long pilotoId = registrarPilotoDePrueba();

        mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSancion(pilotoId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validacionSinPilotoNiEscuderiaDevuelve400() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        String cuerpo = "{\"categoria\":\"F1\",\"motivo\":\"Prueba\",\"fecha\":\"2026-06-01\"}";
        mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void marcarVistoDevuelve200() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        Long pilotoId = registrarPilotoDePrueba();

        String respuesta = mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSancion(pilotoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(post("/api/sanciones/" + id + "/visto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sancionId").value(id))
                .andExpect(jsonPath("$.visto").value(true));
    }

    @Test
    void listadoConFiltrosDevuelve200() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-12-31"))
                .andExpect(status().isOk());
    }
}
