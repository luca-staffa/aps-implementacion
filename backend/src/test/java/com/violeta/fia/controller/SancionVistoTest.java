package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.Sancion;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    private Long sancionId;

    @BeforeEach
    void sembrar() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();

        Usuario piloto = usuarioRepository.findByUsername("piloto.demo").orElseThrow();
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
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(post("/api/sanciones/" + sancionId + "/visto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sancionId").value(sancionId))
                .andExpect(jsonPath("$.username").value("admin.escuderia"))
                .andExpect(jsonPath("$.fechaVisto").isNotEmpty())
                .andExpect(jsonPath("$.visto").value(true));

        mockMvc.perform(get("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadVistas").value(1))
                .andExpect(jsonPath("$.vistas[0].username").value("admin.escuderia"))
                .andExpect(jsonPath("$.vistas[0].fechaVisto").isNotEmpty());
    }

    @Test
    void marcarVistoDosVecesEsIdempotente() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

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
