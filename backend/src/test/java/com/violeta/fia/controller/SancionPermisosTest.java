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
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SancionPermisosTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private SancionVistoRepository sancionVistoRepository;

    private Long sancionId;
    private String cuerpo;

    @BeforeEach
    void sembrar() {
        sancionVistoRepository.deleteAll();
        sancionRepository.deleteAll();

        Usuario piloto = usuarioRepository.findByUsername("piloto.demo").orElseThrow();
        Sancion sancion = new Sancion();
        sancion.setPiloto(piloto);
        sancion.setCategoria(Categoria.F1);
        sancion.setMotivo("Sancion de prueba para permisos");
        sancion.setFecha(LocalDate.of(2033, 5, 1));
        sancion.setTipo("DEPORTIVA");
        sancionId = sancionRepository.save(sancion).getId();

        cuerpo = "{\"pilotoId\":" + piloto.getId()
                + ",\"categoria\":\"F1\",\"motivo\":\"Editada\",\"fecha\":\"2033-05-02\",\"tipo\":\"DEPORTIVA\"}";
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
}
