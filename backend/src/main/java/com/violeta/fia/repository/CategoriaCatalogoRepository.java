package com.violeta.fia.repository;

import com.violeta.fia.domain.CategoriaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaCatalogoRepository extends JpaRepository<CategoriaCatalogo, Long> {

    Optional<CategoriaCatalogo> findByCodigo(String codigo);

    List<CategoriaCatalogo> findAllByActivaTrueOrderByNombreAsc();
}
