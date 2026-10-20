package com.violeta.fia.service;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.EventoCalendario;
import com.violeta.fia.dto.EventoResponse;
import com.violeta.fia.repository.EventoCalendarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CalendarioService {

    private static final DateTimeFormatter FORMATO_ICS = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter FORMATO_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final EventoCalendarioRepository eventoRepository;

    public CalendarioService(EventoCalendarioRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar(Categoria categoria, Integer anio, LocalDate desde, LocalDate hasta, String circuito) {
        String circuitoLike = circuito == null || circuito.isBlank()
                ? null
                : "%" + circuito.trim().toLowerCase() + "%";
        return eventoRepository.buscarConFiltros(categoria, anio, desde, hasta, circuitoLike)
                .stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(Categoria categoria, Integer anio, LocalDate desde, LocalDate hasta, String circuito) {
        List<EventoResponse> eventos = listar(categoria, anio, desde, hasta, circuito);
        List<String> lineas = eventos.stream()
                .map(e -> e.fechaInicio() + " | " + e.categoria() + " | " + e.nombre()
                        + " | " + (e.circuito() != null ? e.circuito() : "-"))
                .toList();
        return PdfGenerator.generar("Calendario FIA 2026", lineas);
    }

    @Transactional(readOnly = true)
    public byte[] exportarIcs(Categoria categoria, Integer anio, LocalDate desde, LocalDate hasta, String circuito) {
        List<EventoResponse> eventos = listar(categoria, anio, desde, hasta, circuito);
        String ahora = LocalDateTime.now().format(FORMATO_TIMESTAMP);

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//FIA Violeta//Calendario//ES\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");

        for (EventoResponse evento : eventos) {
            LocalDate fin = evento.fechaFin() != null ? evento.fechaFin() : evento.fechaInicio();
            LocalDate finExclusivo = fin.plusDays(1);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:evento-").append(evento.id()).append("@fia\r\n");
            sb.append("DTSTAMP:").append(ahora).append("\r\n");
            sb.append("DTSTART;VALUE=DATE:").append(evento.fechaInicio().format(FORMATO_ICS)).append("\r\n");
            sb.append("DTEND;VALUE=DATE:").append(finExclusivo.format(FORMATO_ICS)).append("\r\n");
            sb.append("SUMMARY:").append(limpiar(evento.categoria() + " - " + evento.nombre())).append("\r\n");
            if (evento.circuito() != null) {
                sb.append("LOCATION:").append(limpiar(evento.circuito())).append("\r\n");
            }
            sb.append("DESCRIPTION:").append(limpiar(evento.tipo() != null ? evento.tipo() : evento.nombre())).append("\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", " ");
    }

    private EventoCalendario buscarEntidad(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evento no encontrado con id: " + id));
    }

    private EventoResponse aResponse(EventoCalendario evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getCategoria(),
                evento.getNombre(),
                evento.getCircuito(),
                evento.getPais(),
                evento.getFechaInicio(),
                evento.getFechaFin(),
                evento.getTipo());
    }
}
