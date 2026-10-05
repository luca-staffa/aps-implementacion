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
            + "(:categoria is null or e.categoria = :categoria) and "
            + "(:desde is null or e.fechaInicio >= :desde) and "
            + "(:hasta is null or e.fechaFin <= :hasta) and "
            + "(:circuito is null or lower(e.circuito) like lower(concat('%', :circuito, '%'))) "
            + "order by e.fechaInicio asc")
    List<EventoCalendario> buscarConFiltros(@Param("categoria") Categoria categoria,
                                            @Param("desde") LocalDate desde,
                                            @Param("hasta") LocalDate hasta,
                                            @Param("circuito") String circuito);

    List<EventoCalendario> findByCategoriaOrderByFechaInicioAsc(Categoria categoria);

    List<EventoCalendario> findByCircuitoContainingIgnoreCase(String circuito);
}
