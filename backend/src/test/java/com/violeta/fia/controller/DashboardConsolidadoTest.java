package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardConsolidadoTest extends BaseIntegrationTest {

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/dashboard/consolidado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEscuderiaDevuelve403() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminFiaPuedeConsultarElDashboard() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminFiaPuedeFiltrarPorCategoriaYTemporada() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(1)))
                .andExpect(jsonPath("$.categorias[0].codigo").value("F1"))
                .andExpect(jsonPath("$.categorias[0].temporadaActual").value(2026));
    }
}
