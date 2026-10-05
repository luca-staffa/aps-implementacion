package com.violeta.fia.repository;

import com.violeta.fia.domain.Categoria;
import com.violeta.fia.domain.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {

    @Query("select s from Sancion s where "
            + "(:categoria is null or s.categoria = :categoria) and "
            + "(:desde is null or s.fecha >= :desde) and "
            + "(:hasta is null or s.fecha <= :hasta) "
            + "order by s.fecha desc")
    List<Sancion> buscarConFiltros(@Param("categoria") Categoria categoria,
                                   @Param("desde") LocalDate desde,
                                   @Param("hasta") LocalDate hasta);

    List<Sancion> findByPilotoId(Long pilotoId);

    List<Sancion> findByEscuderiaId(Long escuderiaId);
}
