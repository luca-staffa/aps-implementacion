package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.EventoCalendario;
import com.violeta.fia.repository.EventoCalendarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarioFiltrosTest extends BaseIntegrationTest {

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @BeforeEach
    void sembrar() {
        eventoRepository.deleteAll();
        crear(Categoria.F1, "Gran Premio Test Unico", "Circuito Monza Uno", "Testlandia",
                LocalDate.of(2032, 5, 1), LocalDate.of(2032, 5, 3), "CARRERA");
        crear(Categoria.F2, "Ronda Test Dos", "Circuito Imola Dos", "Testlandia",
                LocalDate.of(2032, 6, 1), LocalDate.of(2032, 6, 2), "CARRERA");
    }

    private void crear(Categoria categoria, String nombre, String circuito, String pais,
                       LocalDate inicio, LocalDate fin, String tipo) {
        EventoCalendario evento = new EventoCalendario();
        evento.setCategoria(categoria);
        evento.setNombre(nombre);
        evento.setCircuito(circuito);
        evento.setPais(pais);
        evento.setFechaInicio(inicio);
        evento.setFechaFin(fin);
        evento.setTipo(tipo);
        eventoRepository.save(evento);
    }

    @Test
    void filtraPorCategoria() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos").param("categoria", "F1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("F1"))
                .andExpect(jsonPath("$[0].circuito").value("Circuito Monza Uno"))
                .andExpect(jsonPath("$[0].fechaInicio").value("2032-05-01"));
    }

    @Test
    void filtraPorCircuito() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos").param("circuito", "Imola"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("F2"))
                .andExpect(jsonPath("$[0].circuito").value("Circuito Imola Dos"));
    }

    @Test
    void filtraPorRangoDeFechas() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos")
                        .param("desde", "2032-05-15")
                        .param("hasta", "2032-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("F2"));
    }

    @Test
    void exportaIcsContieneElEvento() throws Exception {
        mockMvc.perform(get("/api/calendario/eventos/export.ics").param("categoria", "F1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BEGIN:VEVENT")))
                .andExpect(content().string(containsString("Gran Premio Test Unico")))
                .andExpect(content().string(containsString("LOCATION:Circuito Monza Uno")));
    }
}
