package com.violeta.fia.repository;

import com.violeta.fia.domain.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemporadaRepository extends JpaRepository<Temporada, Long> {

    List<Temporada> findAllByActivaTrueOrderByAnioDesc();

    Optional<Temporada> findByAnio(Integer anio);
}
