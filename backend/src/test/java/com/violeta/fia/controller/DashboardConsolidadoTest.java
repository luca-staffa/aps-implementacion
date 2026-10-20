package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.TemporadaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardConsolidadoTest extends BaseIntegrationTest {

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Autowired
    private TemporadaRepository temporadaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    private CategoriaCatalogo categoria;
    private Temporada temporada;
    private Escuderia escuderia;

    @BeforeEach
    void setUp() {
        categoria = categoriaCatalogoRepository.findByCodigo("F1")
                .orElseGet(() -> categoriaCatalogoRepository.save(new CategoriaCatalogo("F1", "Fórmula 1", true)));
        temporada = temporadaRepository.findByAnio(2026)
                .orElseGet(() -> temporadaRepository.save(new Temporada(2026, "Temporada 2026", true, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))));
        escuderia = escuderiaRepository.findByNombre("Alpine")
                .orElseGet(() -> escuderiaRepository.save(new Escuderia("Alpine", "Francia", true)));
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();
    }

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
        guardarPiloto("Charles", "Leclerc", 16, "LEC", Piloto.RolPiloto.TITULAR);
        guardarPiloto("Carlos", "Sainz", 55, "SAI", Piloto.RolPiloto.SUPLENTE);

        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(1)))
                .andExpect(jsonPath("$.categorias[0].codigo").value("F1"))
                .andExpect(jsonPath("$.categorias[0].temporadaActual").value(2026))
                .andExpect(jsonPath("$.categorias[0].pilotos").value(2));
    }

    private void guardarPiloto(String nombre, String apellido, int numero, String abreviatura, Piloto.RolPiloto rol) {
        Piloto piloto = new Piloto();
        piloto.setNombre(nombre);
        piloto.setApellido(apellido);
        piloto.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        piloto.setNacionalidad("Argentina");
        piloto.setNumero(numero);
        piloto.setAbreviatura(abreviatura);
        piloto.setRol(rol);
        piloto.setNumeroLicenciaFia("FIA-" + numero);
        piloto.setFotoUrl("https://example.com/" + nombre + ".jpg");
        piloto.setActivo(true);
        piloto.setCategoria(categoria);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        pilotoRepository.save(piloto);
    }
}
