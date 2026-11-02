package com.violeta.fia.controller;

import com.violeta.fia.BaseIntegrationTest;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.RolEscuderia;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EscuderiaRolesTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EscuderiaRepository escuderiaRepository;

    private Usuario editorAlpine;
    private Usuario lectorAlpine;
    private Usuario editorFerrari;
    private Long alpineId;

    @BeforeEach
    void sembrar() {
        Escuderia alpine = escuderiaRepository.findByNombre("Alpine").orElseThrow();
        alpineId = alpine.getId();

        editorAlpine = usuarioRepository.findByUsername("editor.alpine").orElseThrow();
        editorAlpine.setEscuderia(alpine);
        editorAlpine.setRolEscuderia(RolEscuderia.EDITOR);
        usuarioRepository.save(editorAlpine);

        lectorAlpine = usuarioRepository.findByUsername("lector.alpine").orElseThrow();
        lectorAlpine.setEscuderia(alpine);
        lectorAlpine.setRolEscuderia(RolEscuderia.LECTOR);
        usuarioRepository.save(lectorAlpine);

        editorFerrari = usuarioRepository.findByUsername("editor.ferrari").orElseThrow();
    }

    @Test
    void adminEscuderiaListaMiembrosDeSuEscuderia() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(get("/api/escuderias/miembros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("editor.alpine")))
                .andExpect(jsonPath("$[*].username", hasItem("lector.alpine")))
                .andExpect(jsonPath("$[*].username", not(hasItem("editor.ferrari"))));
    }

    @Test
    void publicoNoPuedeListarMiembros() throws Exception {
        String token = obtenerToken("publico", "Password123!");

        mockMvc.perform(get("/api/escuderias/miembros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminFiaNoPuedeListarMiembros() throws Exception {
        String token = obtenerToken("admin.fia", "Password123!");

        mockMvc.perform(get("/api/escuderias/miembros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeAsignarRolAMiembroDeSuEscuderia() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(put("/api/escuderias/miembros/" + editorAlpine.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rolEscuderia\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("editor.alpine"))
                .andExpect(jsonPath("$.rolEscuderia").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.puedeActualizarInformacion").value(true));
    }

    @Test
    void adminPuedeRevocarRol() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(delete("/api/escuderias/miembros/" + editorAlpine.getId() + "/rol")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolEscuderia").doesNotExist())
                .andExpect(jsonPath("$.puedeActualizarInformacion").value(false));
    }

    @Test
    void adminNoPuedeAsignarRolAUsuarioDeOtraEscuderia() throws Exception {
        String token = obtenerToken("admin.escuderia", "Password123!");

        mockMvc.perform(put("/api/escuderias/miembros/" + editorFerrari.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rolEscuderia\":\"EDITOR\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void miembroConEditorPuedeActualizarInformacionDeSuEscuderia() throws Exception {
        String token = obtenerToken("editor.alpine", "Password123!");

        mockMvc.perform(put("/api/escuderias/" + alpineId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alpine\",\"pais\":\"Francia actualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pais").value("Francia actualizada"));
    }

    @Test
    void miembroConLectorNoPuedeActualizarInformacion() throws Exception {
        String token = obtenerToken("lector.alpine", "Password123!");

        mockMvc.perform(put("/api/escuderias/" + alpineId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alpine\",\"pais\":\"Intentando cambiar\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeOtraEscuderiaNoPuedeActualizarInformacion() throws Exception {
        String token = obtenerToken("admin.ferrari", "Password123!");

        mockMvc.perform(put("/api/escuderias/" + alpineId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alpine\",\"pais\":\"No permitido\"}"))
                .andExpect(status().isForbidden());
    }
}
