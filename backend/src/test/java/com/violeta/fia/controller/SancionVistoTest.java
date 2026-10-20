package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.Sancion;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.TemporadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SancionVistoTest extends BaseIntegrationTest {

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Autowired
    private TemporadaRepository temporadaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    private Long sancionId;

    @BeforeEach
    void sembrar() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();

        CategoriaCatalogo categoria = categoriaCatalogoRepository.findByCodigo("F1").orElseThrow();
        Temporada temporada = temporadaRepository.findByAnio(2026).orElseThrow();
        Escuderia escuderia = escuderiaRepository.findByNombre("Ferrari").orElseThrow();

        Piloto piloto = new Piloto();
        piloto.setNombre("Piloto");
        piloto.setApellido("Visto");
        piloto.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        piloto.setNacionalidad("Argentina");
        piloto.setNumero(97);
        piloto.setAbreviatura("PVS");
        piloto.setRol(Piloto.RolPiloto.TITULAR);
        piloto.setNumeroLicenciaFia("FIA-PVS-97");
        piloto.setActivo(true);
        piloto.setCategoria(categoria);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        pilotoRepository.save(piloto);

        Sancion sancion = new Sancion();
        sancion.setPiloto(piloto);
        sancion.setCategoria(Categoria.F1);
        sancion.setMotivo("Sancion de prueba para visto");
        sancion.setFecha(LocalDate.of(2033, 4, 1));
        sancion.setTipo("DEPORTIVA");
        sancionId = sancionRepository.save(sancion).getId();
    }

    @Test
    void marcarVistoRegistraFechaYUsuario() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(post("/api/sanciones/" + sancionId + "/visto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sancionId").value(sancionId))
                .andExpect(jsonPath("$.username").value("admin.fia"))
                .andExpect(jsonPath("$.fechaVisto").isNotEmpty())
                .andExpect(jsonPath("$.visto").value(true));

        mockMvc.perform(get("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadVistas").value(1))
                .andExpect(jsonPath("$.vistas[0].username").value("admin.fia"))
                .andExpect(jsonPath("$.vistas[0].fechaVisto").isNotEmpty());
    }

    @Test
    void marcarVistoDosVecesEsIdempotente() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(post("/api/sanciones/" + sancionId + "/visto")
                .header("Authorization", "Bearer " + token));
        mockMvc.perform(post("/api/sanciones/" + sancionId + "/visto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadVistas").value(1));
    }
}
