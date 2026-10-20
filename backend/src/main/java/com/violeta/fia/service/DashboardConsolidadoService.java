package com.violeta.fia.service;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.CategoriaAtributo;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.PruebaNeumatico;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.dto.DashboardConsolidadoResponse;
import com.violeta.fia.repository.CategoriaAtributoRepository;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EventoCalendarioRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.PruebaNeumaticoRepository;
import com.violeta.fia.repository.SancionRepository;
import com.violeta.fia.repository.TemporadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DashboardConsolidadoService {

    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final TemporadaRepository temporadaRepository;
    private final EventoCalendarioRepository eventoCalendarioRepository;
    private final SancionRepository sancionRepository;
    private final PilotoRepository pilotoRepository;
    private final PruebaNeumaticoRepository pruebaNeumaticoRepository;

    public DashboardConsolidadoService(CategoriaCatalogoRepository categoriaCatalogoRepository,
                                       CategoriaAtributoRepository categoriaAtributoRepository,
                                       TemporadaRepository temporadaRepository,
                                       EventoCalendarioRepository eventoCalendarioRepository,
                                       SancionRepository sancionRepository,
                                       PilotoRepository pilotoRepository,
                                       PruebaNeumaticoRepository pruebaNeumaticoRepository) {
        this.categoriaCatalogoRepository = categoriaCatalogoRepository;
        this.categoriaAtributoRepository = categoriaAtributoRepository;
        this.temporadaRepository = temporadaRepository;
        this.eventoCalendarioRepository = eventoCalendarioRepository;
        this.sancionRepository = sancionRepository;
        this.pilotoRepository = pilotoRepository;
        this.pruebaNeumaticoRepository = pruebaNeumaticoRepository;
    }

    /**
     * Vista consolidada de las categorias activas. Cuando {@code temporadaAnio} es {@code null}
     * ("Todas las temporadas") los indicadores agregan todos los anios disponibles, usando
     * exactamente la misma semantica de fechas que el calendario publico y el modulo de pruebas.
     */
    @Transactional(readOnly = true)
    public DashboardConsolidadoResponse consolidado(Categoria categoria, Integer temporadaAnio) {
        List<CategoriaCatalogo> categorias = categoriaCatalogoRepository.findAllByActivaTrueOrderByNombreAsc();
        List<DashboardConsolidadoResponse.CategoriaDashboardItem> items = new ArrayList<>();

        Long temporadaId = temporadaAnio != null
                ? temporadaRepository.findByAnio(temporadaAnio).map(Temporada::getId).orElse(null)
                : null;

        for (CategoriaCatalogo categoriaCatalogo : categorias) {
            if (categoria != null && !categoria.name().equals(categoriaCatalogo.getCodigo())) {
                continue;
            }

            Categoria enumCategoria = parsearCategoria(categoriaCatalogo.getCodigo());
            List<CategoriaAtributo> atributos = categoriaAtributoRepository
                    .findByCategoriaIdOrderByClaveAsc(categoriaCatalogo.getId());

            long eventos = enumCategoria == null
                    ? 0L
                    : eventoCalendarioRepository.contarPorCategoriaYAnio(enumCategoria, temporadaAnio);
            long sanciones = enumCategoria == null
                    ? 0L
                    : sancionRepository.contarActivasPorCategoriaYAnio(enumCategoria, temporadaAnio);
            long pruebas = contarPruebas(categoriaCatalogo.getCodigo(), temporadaAnio);
            long pilotos = contarPilotos(categoriaCatalogo.getId(), temporadaId, temporadaAnio);

            items.add(new DashboardConsolidadoResponse.CategoriaDashboardItem(
                    categoriaCatalogo.getId(),
                    categoriaCatalogo.getCodigo(),
                    categoriaCatalogo.getNombre(),
                    temporadaAnio,
                    eventos,
                    sanciones,
                    pruebas,
                    pilotos,
                    atributos.stream()
                            .map(atributo -> new DashboardConsolidadoResponse.AtributoCategoriaItem(
                                    atributo.getClave(),
                                    atributo.getValor(),
                                    atributo.getDescripcion()))
                            .toList()));
        }

        return new DashboardConsolidadoResponse(items);
    }

    private Categoria parsearCategoria(String codigo) {
        try {
            return Categoria.valueOf(codigo);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }

    private long contarPilotos(Long categoriaId, Long temporadaId, Integer temporadaAnio) {
        if (temporadaAnio == null) {
            return pilotoRepository.countByActivoTrueAndCategoriaId(categoriaId);
        }
        if (temporadaId == null) {
            return 0L;
        }
        return pilotoRepository.countByActivoTrueAndCategoriaIdAndTemporadaId(categoriaId, temporadaId);
    }

    private long contarPruebas(String codigoCategoria, Integer anio) {
        List<PruebaNeumatico> pruebas = anio != null
                ? pruebaNeumaticoRepository.findByAnioOrderByFechaInicioAsc(anio)
                : pruebaNeumaticoRepository.findAll();
        return pruebas.stream()
                .filter(prueba -> incluyeCategoria(prueba.getCategorias(), codigoCategoria))
                .count();
    }

    private boolean incluyeCategoria(String categorias, String codigo) {
        if (categorias == null || codigo == null) {
            return false;
        }
        return Arrays.stream(categorias.split(","))
                .map(String::trim)
                .anyMatch(token -> token.equalsIgnoreCase(codigo));
    }
}
