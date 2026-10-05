package com.violeta.fia.repository;

import com.violeta.fia.domain.ResultadoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoPruebaRepository extends JpaRepository<ResultadoPrueba, Long> {

    List<ResultadoPrueba> findByPruebaIdOrderByPosicionAsc(Long pruebaId);

    List<ResultadoPrueba> findByPruebaIdAndEscuderiaContainingIgnoreCase(Long pruebaId, String escuderia);
}
