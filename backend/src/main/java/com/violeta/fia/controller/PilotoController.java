package com.violeta.fia.controller;

import com.violeta.fia.dto.PilotoPublicResponse;
import com.violeta.fia.dto.PilotoRequest;
import com.violeta.fia.dto.PilotoResponse;
import com.violeta.fia.service.PilotoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pilotos")
public class PilotoController {

    private final PilotoService pilotoService;

    public PilotoController(PilotoService pilotoService) {
        this.pilotoService = pilotoService;
    }

    @GetMapping
    public ResponseEntity<List<PilotoResponse>> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long temporadaId,
            @RequestParam(required = false) Integer temporada,
            Authentication authentication) {
        return ResponseEntity.ok(
                pilotoService.listar(authentication, categoriaId, categoria, temporadaId, temporada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PilotoResponse> obtener(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(pilotoService.obtener(id, authentication));
    }

    @PostMapping
    public ResponseEntity<PilotoResponse> crear(@Valid @RequestBody PilotoRequest request,
                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pilotoService.crear(request, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PilotoResponse> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody PilotoRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.ok(pilotoService.actualizar(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        pilotoService.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/publico")
    public ResponseEntity<List<PilotoPublicResponse>> listarPublico(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long temporadaId,
            @RequestParam(required = false) Long escuderiaId,
            @RequestParam(required = false) String escuderia) {
        return ResponseEntity.ok(pilotoService.listarPublico(categoriaId, temporadaId, escuderiaId, escuderia));
    }
}
