package com.violeta.fia.service;

import com.violeta.fia.domain.ControlTecnico;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.dto.ControlTecnicoRequest;
import com.violeta.fia.dto.ControlTecnicoResponse;
import com.violeta.fia.repository.ControlTecnicoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.PilotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ControlTecnicoService {

    private final ControlTecnicoRepository controlTecnicoRepository;
    private final EscuderiaRepository escuderiaRepository;
    private final PilotoRepository pilotoRepository;

    public ControlTecnicoService(ControlTecnicoRepository controlTecnicoRepository,
                                 EscuderiaRepository escuderiaRepository,
                                 PilotoRepository pilotoRepository) {
        this.controlTecnicoRepository = controlTecnicoRepository;
        this.escuderiaRepository = escuderiaRepository;
        this.pilotoRepository = pilotoRepository;
    }

    @Transactional(readOnly = true)
    public List<ControlTecnicoResponse> listar(Long escuderiaId) {
        List<ControlTecnico> controles = (escuderiaId != null)
                ? controlTecnicoRepository.findByEscuderiaIdOrderByFechaDescIdDesc(escuderiaId)
                : controlTecnicoRepository.findAllByOrderByFechaDescIdDesc();
        return controles.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public ControlTecnicoResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    @Transactional
    public ControlTecnicoResponse crear(ControlTecnicoRequest request) {
        ControlTecnico control = new ControlTecnico();
        aplicar(control, request);
        return aResponse(controlTecnicoRepository.save(control));
    }

    @Transactional
    public ControlTecnicoResponse actualizar(Long id, ControlTecnicoRequest request) {
        ControlTecnico control = buscarEntidad(id);
        aplicar(control, request);
        return aResponse(controlTecnicoRepository.save(control));
    }

    private ControlTecnico buscarEntidad(Long id) {
        return controlTecnicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Control tecnico no encontrado con id: " + id));
    }

    private void aplicar(ControlTecnico control, ControlTecnicoRequest request) {
        Escuderia escuderia = escuderiaRepository.findById(request.escuderiaId())
                .orElseThrow(() -> new IllegalArgumentException("Escuderia no encontrada con id: " + request.escuderiaId()));

        Piloto piloto = null;
        if (request.pilotoId() != null) {
            piloto = pilotoRepository.findById(request.pilotoId())
                    .orElseThrow(() -> new IllegalArgumentException("Piloto no encontrado con id: " + request.pilotoId()));
        }

        control.setEscuderia(escuderia);
        control.setPiloto(piloto);
        control.setAuto(request.auto());
        control.setFecha(request.fecha());
        control.setElementosVerificados(request.elementosVerificados());
        control.setResultado(request.resultado());
        control.setObservaciones(request.observaciones());
    }

    private ControlTecnicoResponse aResponse(ControlTecnico control) {
        return new ControlTecnicoResponse(
                control.getId(),
                control.getEscuderia() != null ? control.getEscuderia().getId() : null,
                control.getEscuderia() != null ? control.getEscuderia().getNombre() : null,
                control.getPiloto() != null ? control.getPiloto().getId() : null,
                control.getPiloto() != null ? nombrePiloto(control.getPiloto()) : null,
                control.getAuto(),
                control.getFecha(),
                control.getElementosVerificados(),
                control.getResultado() != null ? control.getResultado().name() : null,
                control.getObservaciones(),
                control.getFechaCreacion());
    }

    private String nombrePiloto(Piloto piloto) {
        String nombre = piloto.getNombre() != null ? piloto.getNombre() : "";
        String apellido = piloto.getApellido() != null ? piloto.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }
}
