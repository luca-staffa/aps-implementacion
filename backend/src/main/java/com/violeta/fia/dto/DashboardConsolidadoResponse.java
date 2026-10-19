package com.violeta.fia.dto;

import java.util.List;

public record DashboardConsolidadoResponse(
        List<CategoriaDashboardItem> categorias
) {
    public record CategoriaDashboardItem(
            Long categoriaId,
            String codigo,
            String nombre,
            Integer temporadaActual,
            Long eventosCalendario,
            Long sanciones,
            Long pruebasNeumaticos,
            Long pilotos,
            List<AtributoCategoriaItem> atributos
    ) {
    }

    public record AtributoCategoriaItem(
            String clave,
            String valor,
            String descripcion
    ) {
    }
}
