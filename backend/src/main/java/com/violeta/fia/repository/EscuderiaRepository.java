package com.violeta.fia.repository;

import com.violeta.fia.domain.Escuderia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EscuderiaRepository extends JpaRepository<Escuderia, Long> {

    Optional<Escuderia> findByNombre(String nombre);

    List<Escuderia> findAllByActivaTrueOrderByNombreAsc();
}
