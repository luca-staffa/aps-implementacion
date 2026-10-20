package com.violeta.fia.controller;

import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.dto.EscuderiaResponse;
import com.violeta.fia.repository.EscuderiaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catalogo de escuderias activas. Se expone en modo lectura para poblar los
 * selectores del modulo de sanciones.
 */
@RestController
@RequestMapping("/api/escuderias")
public class EscuderiaController {

    private final EscuderiaRepository escuderiaRepository;

    public EscuderiaController(EscuderiaRepository escuderiaRepository) {
        this.escuderiaRepository = escuderiaRepository;
    }

    @GetMapping
    public ResponseEntity<List<EscuderiaResponse>> listar() {
        return ResponseEntity.ok(escuderiaRepository.findAllByActivaTrueOrderByNombreAsc().stream()
                .map(this::aResponse)
                .toList());
    }

    private EscuderiaResponse aResponse(Escuderia escuderia) {
        return new EscuderiaResponse(
                escuderia.getId(),
                escuderia.getNombre(),
                escuderia.getPais(),
                escuderia.isActiva());
    }
}
