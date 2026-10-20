package com.violeta.fia.repository;

import com.violeta.fia.domain.Piloto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PilotoRepository extends JpaRepository<Piloto, Long> {

    List<Piloto> findAllByActivoTrueOrderByApellidoAsc();

    List<Piloto> findByActivoTrueAndEscuderiaIdOrderByApellidoAsc(Long escuderiaId);

    List<Piloto> findByActivoTrueAndCategoriaIdAndTemporadaIdOrderByApellidoAsc(Long categoriaId, Long temporadaId);

    List<Piloto> findByActivoTrueAndCategoriaIdAndTemporadaIdAndEscuderiaIdOrderByApellidoAsc(Long categoriaId, Long temporadaId, Long escuderiaId);

    List<Piloto> findByEscuderiaIdAndActivoTrueAndCategoriaIdAndTemporadaId(Long escuderiaId, Long categoriaId, Long temporadaId);

    Optional<Piloto> findByCategoriaIdAndTemporadaIdAndNumero(Long categoriaId, Long temporadaId, Integer numero);

    Optional<Piloto> findByCategoriaIdAndTemporadaIdAndAbreviaturaIgnoreCase(Long categoriaId, Long temporadaId, String abreviatura);

    long countByActivoTrueAndCategoriaIdAndTemporadaId(Long categoriaId, Long temporadaId);

    long countByEscuderiaIdAndActivoTrueAndCategoriaIdAndTemporadaIdAndRol(Long escuderiaId, Long categoriaId, Long temporadaId, Piloto.RolPiloto rol);
}
