package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.EventoCalendario;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.EventoCalendarioRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.TemporadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SancionControllerTest extends BaseIntegrationTest {

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Autowired
    private TemporadaRepository temporadaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    private Long eventoId;

    @BeforeEach
    void crearCarrera() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();

        EventoCalendario evento = new EventoCalendario();
        evento.setCategoria(Categoria.F1);
        evento.setNombre("Carrera de prueba de sanciones");
        evento.setCircuito("Circuito de prueba");
        evento.setPais("Testlandia");
        evento.setFechaInicio(LocalDate.of(2033, 6, 1));
        evento.setFechaFin(LocalDate.of(2033, 6, 2));
        evento.setTipo("CARRERA");
        eventoId = eventoRepository.save(evento).getId();
    }

    private Long registrarPilotoDePrueba() {
        CategoriaCatalogo categoria = categoriaCatalogoRepository.findByCodigo("F1").orElseThrow();
        Temporada temporada = temporadaRepository.findByAnio(2026).orElseThrow();
        Escuderia escuderia = escuderiaRepository.findByNombre("Ferrari").orElseThrow();

        Piloto piloto = new Piloto();
        piloto.setNombre("Piloto");
        piloto.setApellido("Prueba");
        piloto.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        piloto.setNacionalidad("Argentina");
        piloto.setNumero(99);
        piloto.setAbreviatura("TST");
        piloto.setRol(Piloto.RolPiloto.TITULAR);
        piloto.setNumeroLicenciaFia("FIA-TST-99");
        piloto.setActivo(true);
        piloto.setCategoria(categoria);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        return pilotoRepository.save(piloto).getId();
    }

    private String cuerpoSancion(Long pilotoId) {
        return "{\"pilotoId\":" + pilotoId + ",\"categoria\":\"F1\",\"eventoId\":" + eventoId
                + ",\"motivo\":\"Prueba\",\"fecha\":\"2026-06-01\",\"tipo\":\"DEPORTIVA\","
                + "\"descripcion\":\"Prueba\"}";
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

        String cuerpo = "{\"categoria\":\"F1\",\"eventoId\":" + eventoId
                + ",\"motivo\":\"Prueba\",\"fecha\":\"2026-06-01\",\"tipo\":\"DEPORTIVA\"}";
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

    @Test
    void validacionSinCategoriaDevuelve400() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        Long pilotoId = registrarPilotoDePrueba();

        String cuerpo = "{\"pilotoId\":" + pilotoId + ",\"eventoId\":" + eventoId
                + ",\"motivo\":\"Prueba\",\"fecha\":\"2026-06-01\",\"tipo\":\"DEPORTIVA\"}";
        mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validacionSinPenalidadDevuelve400() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        Long pilotoId = registrarPilotoDePrueba();

        String cuerpo = "{\"pilotoId\":" + pilotoId + ",\"categoria\":\"F1\",\"eventoId\":" + eventoId
                + ",\"motivo\":\"Prueba\",\"fecha\":\"2026-06-01\"}";
        mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validacionSinCarreraDevuelve400() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        Long pilotoId = registrarPilotoDePrueba();

        String cuerpo = "{\"pilotoId\":" + pilotoId + ",\"categoria\":\"F1\",\"motivo\":\"Prueba\","
                + "\"fecha\":\"2026-06-01\",\"tipo\":\"DEPORTIVA\"}";
        mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }
}
