package com.violeta.fia.repository;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {

    @Query("select s from Sancion s where s.activo = true and "
            + "s.categoria = coalesce(:categoria, s.categoria) and "
            + "s.fecha >= coalesce(:desde, s.fecha) and "
            + "s.fecha <= coalesce(:hasta, s.fecha) and "
            + "coalesce(s.piloto.id, -1) = coalesce(:pilotoId, coalesce(s.piloto.id, -1)) and "
            + "coalesce(s.escuderia.id, -1) = coalesce(:escuderiaId, coalesce(s.escuderia.id, -1)) and "
            + "coalesce(s.evento.id, -1) = coalesce(:eventoId, coalesce(s.evento.id, -1)) "
            + "order by s.fecha desc")
    List<Sancion> buscarConFiltros(@Param("categoria") Categoria categoria,
                                   @Param("desde") LocalDate desde,
                                   @Param("hasta") LocalDate hasta,
                                   @Param("pilotoId") Long pilotoId,
                                   @Param("escuderiaId") Long escuderiaId,
                                   @Param("eventoId") Long eventoId);

    List<Sancion> findByPilotoIdAndActivoTrue(Long pilotoId);

    List<Sancion> findByEscuderiaIdAndActivoTrue(Long escuderiaId);

    List<Sancion> findByEventoIdAndActivoTrue(Long eventoId);
}
