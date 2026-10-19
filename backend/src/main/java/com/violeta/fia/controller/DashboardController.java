package com.violeta.fia.controller;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.dto.DashboardConsolidadoResponse;
import com.violeta.fia.service.DashboardConsolidadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardConsolidadoService dashboardConsolidadoService;

    public DashboardController(DashboardConsolidadoService dashboardConsolidadoService) {
        this.dashboardConsolidadoService = dashboardConsolidadoService;
    }

    @GetMapping("/dashboard/consolidado")
    public ResponseEntity<DashboardConsolidadoResponse> consolidado(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) Integer temporada) {
        return ResponseEntity.ok(dashboardConsolidadoService.consolidado(categoria, temporada));
    }
}
