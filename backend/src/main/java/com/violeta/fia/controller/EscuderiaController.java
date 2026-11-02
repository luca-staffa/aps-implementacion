package com.violeta.fia.controller;

import com.violeta.fia.dto.AsignarRolRequest;
import com.violeta.fia.dto.EscuderiaRequest;
import com.violeta.fia.dto.EscuderiaResponse;
import com.violeta.fia.dto.MiembroEscuderiaResponse;
import com.violeta.fia.service.EscuderiaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catalogo de escuderias y gestion de los roles internos de sus miembros.
 * La asignacion de roles queda restringida al administrativo de la escuderia,
 * que solo puede operar dentro de la suya.
 */
@RestController
@RequestMapping("/api/escuderias")
public class EscuderiaController {

    private final EscuderiaService escuderiaService;

    public EscuderiaController(EscuderiaService escuderiaService) {
        this.escuderiaService = escuderiaService;
    }

    @GetMapping
    public ResponseEntity<List<EscuderiaResponse>> listar() {
        return ResponseEntity.ok(escuderiaService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EscuderiaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody EscuderiaRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(escuderiaService.actualizar(id, request, authentication));
    }

    @GetMapping("/miembros")
    public ResponseEntity<List<MiembroEscuderiaResponse>> listarMiembros(Authentication authentication) {
        return ResponseEntity.ok(escuderiaService.listarMiembros(authentication));
    }

    @PutMapping("/miembros/{usuarioId}")
    public ResponseEntity<MiembroEscuderiaResponse> asignarRol(@PathVariable Long usuarioId,
                                                               @Valid @RequestBody AsignarRolRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.ok(escuderiaService.asignarRol(usuarioId, request.rolEscuderia(), authentication));
    }

    @DeleteMapping("/miembros/{usuarioId}/rol")
    public ResponseEntity<MiembroEscuderiaResponse> revocarRol(@PathVariable Long usuarioId,
                                                               Authentication authentication) {
        return ResponseEntity.ok(escuderiaService.revocarRol(usuarioId, authentication));
    }
}
