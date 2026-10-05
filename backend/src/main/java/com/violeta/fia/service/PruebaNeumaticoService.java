package com.violeta.fia.service;

import com.violeta.fia.domain.PruebaNeumatico;
import com.violeta.fia.domain.ResultadoPrueba;
import com.violeta.fia.dto.PruebaResponse;
import com.violeta.fia.dto.ResultadoResponse;
import com.violeta.fia.repository.PruebaNeumaticoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PruebaNeumaticoService {

    private final PruebaNeumaticoRepository pruebaRepository;

    public PruebaNeumaticoService(PruebaNeumaticoRepository pruebaRepository) {
        this.pruebaRepository = pruebaRepository;
    }

    @Transactional(readOnly = true)
    public List<PruebaResponse> listar(Integer anio) {
        List<PruebaNeumatico> pruebas = (anio != null)
                ? pruebaRepository.findByAnioOrderByFechaInicioAsc(anio)
                : pruebaRepository.findAllByOrderByAnioDescFechaInicioAsc();
        return pruebas.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public PruebaResponse obtener(Long id) {
        PruebaNeumatico prueba = pruebaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Prueba no encontrada con id: " + id));
        return aResponse(prueba);
    }

    private PruebaResponse aResponse(PruebaNeumatico prueba) {
        List<ResultadoResponse> resultados = prueba.getResultados().stream()
                .map(this::aResultado)
                .toList();

        return new PruebaResponse(
                prueba.getId(),
                prueba.getAnio(),
                prueba.getCircuito(),
                prueba.getPais(),
                prueba.getFechaInicio(),
                prueba.getFechaFin(),
                prueba.getCategorias(),
                prueba.isAvisoPublicado(),
                resultados);
    }

    private ResultadoResponse aResultado(ResultadoPrueba resultado) {
        return new ResultadoResponse(
                resultado.getId(),
                resultado.getPrueba() != null ? resultado.getPrueba().getId() : null,
                resultado.getEscuderia(),
                resultado.getPiloto(),
                resultado.getMejorTiempo(),
                resultado.getVueltas(),
                resultado.getPosicion());
    }
}
