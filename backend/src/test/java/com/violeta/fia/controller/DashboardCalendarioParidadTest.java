package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.EventoCalendario;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.PruebaNeumatico;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.EventoCalendarioRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.PruebaNeumaticoRepository;
import com.violeta.fia.repository.ResultadoPruebaRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.SancionVistoRepository;
import com.violeta.fia.repository.TemporadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que la vista consolidada y el calendario expongan exactamente los mismos hechos:
 * para una misma categoria y anio, el KPI {@code eventosCalendario} debe coincidir con la
 * cantidad de eventos devueltos por el calendario.
 */
class DashboardCalendarioParidadTest extends BaseIntegrationTest {

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    @Autowired
    private PilotoRepository pilotoRepository;

    @Autowired
    private PruebaNeumaticoRepository pruebaRepository;

    @Autowired
    private ResultadoPruebaRepository resultadoRepository;

    @Autowired
    private CategoriaCatalogoRepository categoriaCatalogoRepository;

    @Autowired
    private TemporadaRepository temporadaRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    private CategoriaCatalogo f1;

    @BeforeEach
    void sembrar() {
        resultadoRepository.deleteAll();
        pruebaRepository.deleteAll();
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        pilotoRepository.deleteAll();
        eventoRepository.deleteAll();

        f1 = categoriaCatalogoRepository.findByCodigo("F1").orElseThrow();
        temporadaRepository.findByAnio(2027)
                .orElseGet(() -> temporadaRepository.save(
                        new Temporada(2027, "Temporada 2027", true, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31))));

        crearEvento(Categoria.F1, "GP F1 2026", LocalDate.of(2026, 4, 10));
        crearEvento(Categoria.F1, "GP F1 2027 A", LocalDate.of(2027, 3, 5));
        crearEvento(Categoria.F1, "GP F1 2027 B", LocalDate.of(2027, 9, 12));
        crearEvento(Categoria.F2, "GP F2 2027", LocalDate.of(2027, 5, 20));

        crearPrueba(2027, "Circuito Test", "F1,F2");
    }

    @org.junit.jupiter.api.AfterEach
    void limpiar() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();
        resultadoRepository.deleteAll();
        pruebaRepository.deleteAll();
    }

    @Test
    void elConsolidadoCoincideConElCalendarioParaElMismoAnio() throws Exception {
        int eventosCalendario2027 = contarCalendario(2027);
        assertThat(eventosCalendario2027).isEqualTo(3);

        String token = obtenerToken("admin.fia", "Password123!");
        String respuesta = mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2027"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long eventosF1 = objectMapper.readTree(respuesta)
                .get("categorias").get(0).get("eventosCalendario").asLong();

        assertThat(eventosF1).isEqualTo(contarCalendarioPorCategoria(2027, Categoria.F1));
        assertThat(eventosF1).isEqualTo(2);
    }

    @Test
    void unaTemporadaSinEventosDevuelveCeroSinRomperElMensajeDeVacio() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].eventosCalendario").value(0))
                .andExpect(jsonPath("$.categorias[0].temporada").value(2025));

        mockMvc.perform(get("/api/calendario/eventos")
                        .param("categoria", "F1")
                        .param("anio", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void elKpiDePruebasDesglosaElCsvSinConfundirF1ConF1Academy() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        String respuesta = mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("temporada", "2027"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var categorias = objectMapper.readTree(respuesta).get("categorias");
        for (var categoria : categorias) {
            String codigo = categoria.get("codigo").asText();
            long pruebas = categoria.get("pruebasNeumaticos").asLong();
            if ("F1".equals(codigo) || "F2".equals(codigo)) {
                assertThat(pruebas).as("pruebas de " + codigo).isEqualTo(1);
            } else {
                assertThat(pruebas).as("pruebas de " + codigo).isZero();
            }
        }
    }

    @Test
    void todasLasTemporadasAgregaLosEventosDeTodosLosAnios() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        String respuesta = mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].temporada").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        long eventos = objectMapper.readTree(respuesta)
                .get("categorias").get(0).get("eventosCalendario").asLong();

        assertThat(eventos).isEqualTo(contarCalendarioPorCategoria(null, Categoria.F1));
        assertThat(eventos).isEqualTo(3);
    }

    @Test
    void lasSancionesEliminadasLogicamenteNoSeCuentan() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");
        Long eventoId = eventoRepository.findAll().stream()
                .filter(evento -> evento.getCategoria() == Categoria.F1)
                .findFirst().orElseThrow().getId();

        String cuerpo = "{\"escuderiaId\":" + escuderiaRepository.findByNombre("Ferrari").orElseThrow().getId()
                + ",\"categoria\":\"F1\",\"eventoId\":" + eventoId
                + ",\"motivo\":\"Prueba de paridad\",\"fecha\":\"2027-04-01\",\"tipo\":\"TECNICA\"}";

        String creada = mockMvc.perform(post("/api/sanciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long sancionId = objectMapper.readTree(creada).get("id").asLong();

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2027"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].sanciones").value(1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/sanciones/" + sancionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2027"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].sanciones").value(0));
    }

    @Test
    void losPilotosSeCuentanParaLaTemporadaConsultada() throws Exception {
        Temporada temporada = temporadaRepository.findByAnio(2027).orElseThrow();
        Escuderia escuderia = escuderiaRepository.findByNombre("Ferrari").orElseThrow();

        Piloto piloto = new Piloto();
        piloto.setNombre("Piloto");
        piloto.setApellido("Paridad");
        piloto.setFechaNacimiento(LocalDate.of(1998, 2, 2));
        piloto.setNacionalidad("Argentina");
        piloto.setNumero(77);
        piloto.setAbreviatura("PAR");
        piloto.setRol(Piloto.RolPiloto.TITULAR);
        piloto.setNumeroLicenciaFia("FIA-PAR-77");
        piloto.setActivo(true);
        piloto.setCategoria(f1);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        pilotoRepository.save(piloto);

        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2027"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].pilotos").value(1));

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1")
                        .param("temporada", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].pilotos").value(0));

        mockMvc.perform(get("/api/dashboard/consolidado")
                        .header("Authorization", "Bearer " + token)
                        .param("categoria", "F1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].pilotos").value(1));
    }

    private int contarCalendario(Integer anio) throws Exception {
        String cuerpo = mockMvc.perform(get("/api/calendario/eventos")
                        .param("anio", String.valueOf(anio)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(cuerpo).size();
    }

    private int contarCalendarioPorCategoria(Integer anio, Categoria categoria) throws Exception {
        var peticion = get("/api/calendario/eventos").param("categoria", categoria.name());
        if (anio != null) {
            peticion = peticion.param("anio", String.valueOf(anio));
        }
        String cuerpo = mockMvc.perform(peticion)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(cuerpo).size();
    }

    private void crearEvento(Categoria categoria, String nombre, LocalDate inicio) {
        EventoCalendario evento = new EventoCalendario();
        evento.setCategoria(categoria);
        evento.setNombre(nombre);
        evento.setCircuito("Circuito " + nombre);
        evento.setPais("Testlandia");
        evento.setFechaInicio(inicio);
        evento.setFechaFin(inicio.plusDays(2));
        evento.setTipo("CARRERA");
        eventoRepository.save(evento);
    }

    private void crearPrueba(int anio, String circuito, String categorias) {
        PruebaNeumatico prueba = new PruebaNeumatico();
        prueba.setAnio(anio);
        prueba.setCircuito(circuito);
        prueba.setPais("Testlandia");
        prueba.setFechaInicio(LocalDate.of(anio, 2, 10));
        prueba.setFechaFin(LocalDate.of(anio, 2, 12));
        prueba.setCategorias(categorias);
        prueba.setAvisoPublicado(true);
        pruebaRepository.save(prueba);
    }
}
