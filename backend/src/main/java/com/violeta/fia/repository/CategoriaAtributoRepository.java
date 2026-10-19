package com.violeta.fia.repository;

import com.violeta.fia.domain.CategoriaAtributo;
import com.violeta.fia.domain.CategoriaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaAtributoRepository extends JpaRepository<CategoriaAtributo, Long> {

    List<CategoriaAtributo> findByCategoriaOrderByClaveAsc(CategoriaCatalogo categoria);

    List<CategoriaAtributo> findByCategoriaIdOrderByClaveAsc(Long categoriaId);
}
