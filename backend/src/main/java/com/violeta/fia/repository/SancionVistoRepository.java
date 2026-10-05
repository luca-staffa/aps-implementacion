package com.violeta.fia.repository;

import com.violeta.fia.domain.SancionVisto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SancionVistoRepository extends JpaRepository<SancionVisto, Long> {

    Optional<SancionVisto> findBySancionIdAndUsuarioId(Long sancionId, Long usuarioId);

    boolean existsBySancionIdAndUsuarioId(Long sancionId, Long usuarioId);

    long countBySancionId(Long sancionId);
}
