package com.violeta.fia.repository;

import com.violeta.fia.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    List<Permiso> findByRolId(Long rolId);
}
