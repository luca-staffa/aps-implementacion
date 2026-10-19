package com.violeta.fia.service;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.CategoriaAtributo;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.dto.DashboardConsolidadoResponse;
import com.violeta.fia.repository.CategoriaAtributoRepository;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EventoCalendarioRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.TemporadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardConsolidadoService {

    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final TemporadaRepository temporadaRepository;
    private final EventoCalendarioRepository eventoCalendarioRepository;
    private final SancionRepository sancionRepository;

    public DashboardConsolidadoService(CategoriaCatalogoRepository categoriaCatalogoRepository,
                                       CategoriaAtributoRepository categoriaAtributoRepository,
                                       TemporadaRepository temporadaRepository,
                                       EventoCalendarioRepository eventoCalendarioRepository,
                                       SancionRepository sancionRepository) {
        this.categoriaCatalogoRepository = categoriaCatalogoRepository;
        this.categoriaAtributoRepository = categoriaAtributoRepository;
        this.temporadaRepository = temporadaRepository;
        this.eventoCalendarioRepository = eventoCalendarioRepository;
        this.sancionRepository = sancionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardConsolidadoResponse consolidado(Categoria categoria, Integer temporadaAnio) {
        List<CategoriaCatalogo> categorias = categoriaCatalogoRepository.findAllByActivaTrueOrderByNombreAsc();
        List<DashboardConsolidadoResponse.CategoriaDashboardItem> items = new ArrayList<>();

        Integer temporadaActual = temporadaAnio != null ? temporadaAnio : detectarTemporadaActual();

        for (CategoriaCatalogo categoriaCatalogo : categorias) {
            if (categoria != null && !categoria.name().equals(categoriaCatalogo.getCodigo())) {
                continue;
            }

            List<CategoriaAtributo> atributos = categoriaAtributoRepository.findByCategoriaIdOrderByClaveAsc(categoriaCatalogo.getId());
            long eventos = eventoCalendarioRepository.findAll().stream()
                    .filter(evento -> evento.getCategoria() != null && evento.getCategoria().name().equals(categoriaCatalogo.getCodigo()))
                    .count();
            long sanciones = sancionRepository.findAll().stream()
                    .filter(sancion -> sancion.getCategoria() != null && sancion.getCategoria().name().equals(categoriaCatalogo.getCodigo()))
                    .count();

            items.add(new DashboardConsolidadoResponse.CategoriaDashboardItem(
                    categoriaCatalogo.getId(),
                    categoriaCatalogo.getCodigo(),
                    categoriaCatalogo.getNombre(),
                    temporadaActual,
                    eventos,
                    sanciones,
                    0L,
                    atributos.stream()
                            .map(atributo -> new DashboardConsolidadoResponse.AtributoCategoriaItem(
                                    atributo.getClave(),
                                    atributo.getValor(),
                                    atributo.getDescripcion()))
                            .toList()));
        }

        return new DashboardConsolidadoResponse(items);
    }

    private Integer detectarTemporadaActual() {
        return temporadaRepository.findAllByActivaTrueOrderByAnioDesc().stream()
                .findFirst()
                .map(temporada -> temporada.getAnio())
                .orElse(2026);
    }
}
