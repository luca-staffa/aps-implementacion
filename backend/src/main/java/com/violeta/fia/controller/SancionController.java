package com.violeta.fia.controller;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.dto.SancionRequest;
import com.violeta.fia.dto.SancionResponse;
import com.violeta.fia.dto.VistoResponse;
import com.violeta.fia.service.SancionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sanciones")
public class SancionController {

    private final SancionService sancionService;

    public SancionController(SancionService sancionService) {
        this.sancionService = sancionService;
    }

    @GetMapping
    public ResponseEntity<List<SancionResponse>> listar(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long pilotoId,
            @RequestParam(required = false) Long escuderiaId,
            @RequestParam(required = false) Long eventoId) {
        return ResponseEntity.ok(sancionService.listar(categoria, desde, hasta, pilotoId, escuderiaId, eventoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SancionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(sancionService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<SancionResponse> crear(@Valid @RequestBody SancionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sancionService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SancionResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody SancionRequest request) {
        return ResponseEntity.ok(sancionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sancionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/visto")
    public ResponseEntity<VistoResponse> marcarVisto(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(sancionService.marcarVisto(id, authentication.getName()));
    }
}
