package com.violeta.fia.repository;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.EventoCalendario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, Long> {

    @Query("select e from EventoCalendario e where "
            + "e.categoria = coalesce(:categoria, e.categoria) and "
            + "(:anio is null or year(coalesce(e.fechaInicio, e.fechaFin)) = :anio) and "
            + "coalesce(e.fechaInicio, e.fechaFin) >= coalesce(:desde, coalesce(e.fechaInicio, e.fechaFin)) and "
            + "coalesce(e.fechaFin, e.fechaInicio) <= coalesce(:hasta, coalesce(e.fechaFin, e.fechaInicio)) and "
            + "coalesce(lower(e.circuito), '') like coalesce(:circuitoLike, coalesce(lower(e.circuito), '')) "
            + "order by e.fechaInicio asc")
    List<EventoCalendario> buscarConFiltros(@Param("categoria") Categoria categoria,
                                            @Param("anio") Integer anio,
                                            @Param("desde") LocalDate desde,
                                            @Param("hasta") LocalDate hasta,
                                            @Param("circuitoLike") String circuitoLike);

    /**
     * Cuenta los eventos de una categoria usando exactamente el mismo criterio de anio que
     * {@link #buscarConFiltros} (el anio de la fecha de inicio, o de la de fin si no hay inicio)
     * para que la vista consolidada nunca diverja del calendario.
     */
    @Query("select count(e) from EventoCalendario e where "
            + "e.categoria = :categoria and "
            + "(:anio is null or year(coalesce(e.fechaInicio, e.fechaFin)) = :anio)")
    long contarPorCategoriaYAnio(@Param("categoria") Categoria categoria,
                                 @Param("anio") Integer anio);

    List<EventoCalendario> findByCategoriaOrderByFechaInicioAsc(Categoria categoria);

    List<EventoCalendario> findByCircuitoContainingIgnoreCase(String circuito);
}
