package com.violeta.fia.controller;

import com.violeta.fia.dto.PruebaResponse;
import com.violeta.fia.service.PruebaNeumaticoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pruebas-neumaticos")
public class PruebaNeumaticoController {

    private final PruebaNeumaticoService pruebaService;

    public PruebaNeumaticoController(PruebaNeumaticoService pruebaService) {
        this.pruebaService = pruebaService;
    }

    @GetMapping
    public ResponseEntity<List<PruebaResponse>> listar(@RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(pruebaService.listar(anio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PruebaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pruebaService.obtener(id));
    }
}
