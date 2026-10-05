package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarioControllerTest extends BaseIntegrationTest {

    @Test
    void listadoConFiltrosDevuelve200() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos")
                        .param("categoria", "F1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-12-31")
                        .param("circuito", "Monza"))
                .andExpect(status().isOk());
    }

    @Test
    void exportIcsDevuelve200() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos/export.ics"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/calendar"));
    }

    @Test
    void exportPdfDevuelve200() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos/export.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"));
    }
}
