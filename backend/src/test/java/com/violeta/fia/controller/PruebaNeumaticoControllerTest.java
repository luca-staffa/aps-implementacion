package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.PruebaNeumatico;
import com.violeta.fia.domain.ResultadoPrueba;
import com.violeta.fia.repository.PruebaNeumaticoRepository;
import com.violeta.fia.repository.ResultadoPruebaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PruebaNeumaticoControllerTest extends BaseIntegrationTest {

    @Autowired
    private PruebaNeumaticoRepository pruebaRepository;

    @Autowired
    private ResultadoPruebaRepository resultadoRepository;

    private Long idPrimera;

    @BeforeEach
    void sembrar() {
        resultadoRepository.deleteAll();
        pruebaRepository.deleteAll();

        PruebaNeumatico primera = new PruebaNeumatico();
        primera.setAnio(2031);
        primera.setCircuito("Circuito Norte");
        primera.setPais("Testlandia");
        primera.setFechaInicio(LocalDate.of(2031, 2, 10));
        primera.setFechaFin(LocalDate.of(2031, 2, 12));
        primera.setCategorias("F1,F2");
        primera.setAvisoPublicado(true);
        primera = pruebaRepository.save(primera);

        ResultadoPrueba resultado = new ResultadoPrueba();
        resultado.setPrueba(primera);
        resultado.setEscuderia("Escuderia Alfa");
        resultado.setPiloto("Piloto Uno");
        resultado.setMejorTiempo("1:18.452");
        resultado.setVueltas(62);
        resultado.setPosicion(1);
        resultadoRepository.save(resultado);

        PruebaNeumatico segunda = new PruebaNeumatico();
        segunda.setAnio(2031);
        segunda.setCircuito("Circuito Sur");
        segunda.setPais("Testlandia");
        segunda.setFechaInicio(LocalDate.of(2031, 3, 3));
        segunda.setFechaFin(LocalDate.of(2031, 3, 4));
        segunda.setCategorias("F3,F1_ACADEMY");
        segunda.setAvisoPublicado(false);
        pruebaRepository.save(segunda);

        idPrimera = primera.getId();
    }

    @Test
    void listarPorAnioDevuelveLasDosFechasConResultados() throws Exception {
        mockMvc.perform(get("/api/pruebas-neumaticos").param("anio", "2031"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].circuito").value("Circuito Norte"))
                .andExpect(jsonPath("$[0].fechaInicio").value("2031-02-10"))
                .andExpect(jsonPath("$[0].fechaFin").value("2031-02-12"))
                .andExpect(jsonPath("$[0].categorias").value("F1,F2"))
                .andExpect(jsonPath("$[0].avisoPublicado").value(true))
                .andExpect(jsonPath("$[0].resultados[0].escuderia").value("Escuderia Alfa"))
                .andExpect(jsonPath("$[0].resultados[0].piloto").value("Piloto Uno"))
                .andExpect(jsonPath("$[0].resultados[0].posicion").value(1));
    }

    @Test
    void obtenerPorIdDevuelveLaPrueba() throws Exception {
        mockMvc.perform(get("/api/pruebas-neumaticos/" + idPrimera))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idPrimera))
                .andExpect(jsonPath("$.circuito").value("Circuito Norte"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/pruebas-neumaticos/999999"))
                .andExpect(status().isNotFound());
    }
}
