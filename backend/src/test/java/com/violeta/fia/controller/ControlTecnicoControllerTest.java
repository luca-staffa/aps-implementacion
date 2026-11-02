package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.ControlTecnico;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.ResultadoControl;
import com.violeta.fia.repository.ControlTecnicoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControlTecnicoControllerTest extends BaseIntegrationTest {

    @Autowired
    private ControlTecnicoRepository controlTecnicoRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    private Escuderia ferrari;
    private Escuderia alpine;
    private Long controlId;

    @BeforeEach
    void sembrar() {
        controlTecnicoRepository.deleteAll();
        ferrari = escuderiaRepository.findByNombre("Ferrari").orElseThrow();
        alpine = escuderiaRepository.findByNombre("Alpine").orElseThrow();

        ControlTecnico control = new ControlTecnico();
        control.setEscuderia(ferrari);
        control.setAuto("Ferrari SF-26 #99");
        control.setFecha(LocalDate.of(2040, 3, 14));
        control.setElementosVerificados("Aleron delantero, suelo, unidad de potencia");
        control.setResultado(ResultadoControl.APTO);
        control.setObservaciones("Sin observaciones");
        controlId = controlTecnicoRepository.save(control).getId();
    }

    @Test
    void publicoSinTokenPuedeConsultarControles() throws Exception {
        mockMvc.perform(get("/api/controles-tecnicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].auto").value("Ferrari SF-26 #99"))
                .andExpect(jsonPath("$[0].escuderiaNombre").value("Ferrari"))
                .andExpect(jsonPath("$[0].resultado").value("APTO"));
    }

    @Test
    void consultaFiltraPorEscuderia() throws Exception {
        mockMvc.perform(get("/api/controles-tecnicos").param("escuderiaId", String.valueOf(alpine.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/controles-tecnicos").param("escuderiaId", String.valueOf(ferrari.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/controles-tecnicos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminFiaPuedeRegistrarControl() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        String cuerpo = "{\"escuderiaId\":" + alpine.getId()
                + ",\"auto\":\"Alpine A526 #10\""
                + ",\"fecha\":\"2026-04-02\""
                + ",\"elementosVerificados\":\"Peso minimo, sistema de escape\""
                + ",\"resultado\":\"NO_APTO\""
                + ",\"observaciones\":\"Escape fuera de tolerancia\"}";

        mockMvc.perform(post("/api/controles-tecnicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.escuderiaNombre").value("Alpine"))
                .andExpect(jsonPath("$.resultado").value("NO_APTO"));
    }

    @Test
    void crearSinTokenDevuelve401() throws Exception {
        String cuerpo = "{\"escuderiaId\":" + alpine.getId()
                + ",\"auto\":\"Alpine A526 #10\""
                + ",\"fecha\":\"2040-04-02\""
                + ",\"elementosVerificados\":\"Peso minimo\""
                + ",\"resultado\":\"APTO\"}";

        mockMvc.perform(post("/api/controles-tecnicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearConPublicoDevuelve403() throws Exception {
        String token = obtenerToken("publico", "Password123!");
        String cuerpo = "{\"escuderiaId\":" + alpine.getId()
                + ",\"auto\":\"Alpine A526 #10\""
                + ",\"fecha\":\"2040-04-02\""
                + ",\"elementosVerificados\":\"Peso minimo\""
                + ",\"resultado\":\"APTO\"}";

        mockMvc.perform(post("/api/controles-tecnicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarConEscuderiaDevuelve403() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");
        String cuerpo = "{\"escuderiaId\":" + ferrari.getId()
                + ",\"auto\":\"Ferrari SF-26 #99\""
                + ",\"fecha\":\"2040-03-15\""
                + ",\"elementosVerificados\":\"Suelo\""
                + ",\"resultado\":\"APTO\"}";

        mockMvc.perform(put("/api/controles-tecnicos/" + controlId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void registroConDatosIncompletosDevuelve400() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        String cuerpo = "{\"escuderiaId\":" + alpine.getId()
                + ",\"fecha\":\"2040-04-02\""
                + ",\"resultado\":\"APTO\"}";

        mockMvc.perform(post("/api/controles-tecnicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroConFechaFuturaDevuelve400() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        String cuerpo = "{\"escuderiaId\":" + alpine.getId()
                + ",\"auto\":\"Alpine A526 #10\""
                + ",\"fecha\":\"2999-04-02\""
                + ",\"elementosVerificados\":\"Peso minimo\""
                + ",\"resultado\":\"APTO\"}";

        mockMvc.perform(post("/api/controles-tecnicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }
}
