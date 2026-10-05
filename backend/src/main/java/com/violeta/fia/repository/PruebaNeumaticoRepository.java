package com.violeta.fia.repository;

import com.violeta.fia.domain.PruebaNeumatico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PruebaNeumaticoRepository extends JpaRepository<PruebaNeumatico, Long> {

    List<PruebaNeumatico> findByAnioOrderByFechaInicioAsc(Integer anio);

    List<PruebaNeumatico> findAllByOrderByAnioDescFechaInicioAsc();

    Optional<PruebaNeumatico> findByAnioAndCircuito(Integer anio, String circuito);
}
