package com.violeta.fia.controller;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.dto.EventoResponse;
import com.violeta.fia.service.CalendarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendario/eventos")
public class CalendarioController {

    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping
    public ResponseEntity<List<EventoResponse>> listar(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String circuito) {
        return ResponseEntity.ok(calendarioService.listar(categoria, desde, hasta, circuito));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(calendarioService.obtener(id));
    }

    @GetMapping("/export.pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String circuito) {
        byte[] pdf = calendarioService.exportarPdf(categoria, desde, hasta, circuito);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calendario.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export.ics")
    public ResponseEntity<byte[]> exportarIcs(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String circuito) {
        byte[] ics = calendarioService.exportarIcs(categoria, desde, hasta, circuito);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calendario.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ics);
    }
}
