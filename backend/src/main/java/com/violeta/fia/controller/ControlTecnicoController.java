package com.violeta.fia.controller;

import com.violeta.fia.dto.ControlTecnicoRequest;
import com.violeta.fia.dto.ControlTecnicoResponse;
import com.violeta.fia.service.ControlTecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controles tecnicos de los autos. La consulta es publica (escuderias y
 * publico); el alta y la modificacion quedan restringidas al administrativo
 * de la FIA por la configuracion de seguridad.
 */
@RestController
@RequestMapping("/api/controles-tecnicos")
public class ControlTecnicoController {

    private final ControlTecnicoService controlTecnicoService;

    public ControlTecnicoController(ControlTecnicoService controlTecnicoService) {
        this.controlTecnicoService = controlTecnicoService;
    }

    @GetMapping
    public ResponseEntity<List<ControlTecnicoResponse>> listar(@RequestParam(required = false) Long escuderiaId) {
        return ResponseEntity.ok(controlTecnicoService.listar(escuderiaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ControlTecnicoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(controlTecnicoService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ControlTecnicoResponse> crear(@Valid @RequestBody ControlTecnicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(controlTecnicoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ControlTecnicoResponse> actualizar(@PathVariable Long id,
                                                             @Valid @RequestBody ControlTecnicoRequest request) {
        return ResponseEntity.ok(controlTecnicoService.actualizar(id, request));
    }
}
