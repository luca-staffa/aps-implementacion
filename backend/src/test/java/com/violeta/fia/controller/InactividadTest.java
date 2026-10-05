package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InactividadTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void tokenConActividadRecienteDevuelve200() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tokenVencidoPorInactividadDevuelve401() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        Usuario usuario = usuarioRepository.findByUsername("admin.fia").orElseThrow();
        usuario.setUltimaActividad(LocalDateTime.now().minusMinutes(40));
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
