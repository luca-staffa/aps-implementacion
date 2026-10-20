package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.EventoCalendario;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.Sancion;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SancionPermisosTest extends BaseIntegrationTest {

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Autowired
    private TemporadaRepository temporadaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    private Long sancionId;
    private Long eventoId;
    private String cuerpo;

    @BeforeEach
    void sembrar() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();

        EventoCalendario evento = new EventoCalendario();
        evento.setCategoria(Categoria.F1);
        evento.setNombre("Carrera de prueba de permisos");
        evento.setCircuito("Circuito de permisos");
        evento.setPais("Testlandia");
        evento.setFechaInicio(LocalDate.of(2033, 5, 1));
        evento.setFechaFin(LocalDate.of(2033, 5, 2));
        evento.setTipo("CARRERA");
        eventoId = eventoRepository.save(evento).getId();

        Piloto piloto = crearPiloto();
        Sancion sancion = new Sancion();
        sancion.setPiloto(piloto);
        sancion.setCategoria(Categoria.F1);
        sancion.setEvento(evento);
        sancion.setMotivo("Sancion de prueba para permisos");
        sancion.setFecha(LocalDate.of(2033, 5, 1));
        sancion.setTipo("DEPORTIVA");
        sancionId = sancionRepository.save(sancion).getId();

        cuerpo = "{\"pilotoId\":" + piloto.getId()
                + ",\"categoria\":\"F1\",\"eventoId\":" + eventoId
                + ",\"motivo\":\"Editada\",\"fecha\":\"2033-05-02\",\"tipo\":\"DEPORTIVA\"}";
    }

    private Piloto crearPiloto() {
        CategoriaCatalogo categoria = categoriaCatalogoRepository.findByCodigo("F1").orElseThrow();
        Temporada temporada = temporadaRepository.findByAnio(2026).orElseThrow();
        Escuderia escuderia = escuderiaRepository.findByNombre("Ferrari").orElseThrow();

        Piloto piloto = new Piloto();
        piloto.setNombre("Piloto");
        piloto.setApellido("Permisos");
        piloto.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        piloto.setNacionalidad("Argentina");
        piloto.setNumero(98);
        piloto.setAbreviatura("PMS");
        piloto.setRol(Piloto.RolPiloto.TITULAR);
        piloto.setNumeroLicenciaFia("FIA-PMS-98");
        piloto.setActivo(true);
        piloto.setCategoria(categoria);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        return pilotoRepository.save(piloto);
    }

    @Test
    void actualizarConEscuderiaDevuelve403() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(put("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarConEscuderiaDevuelve403() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(delete("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/sanciones/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarConAdminFiaDevuelve200() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(put("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk());
    }

    @Test
    void publicoNoPuedeListarSanciones() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        mockMvc.perform(get("/api/sanciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEscuderiaNoPuedeListarSanciones() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(get("/api/sanciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicoNoPuedeMarcarVisto() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        mockMvc.perform(post("/api/sanciones/" + sancionId + "/visto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void bajaLogicaOcultaLaSancion() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(delete("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/sanciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
