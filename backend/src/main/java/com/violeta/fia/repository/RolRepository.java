package com.violeta.fia.repository;

import com.violeta.fia.domain.Rol;
import com.violeta.fia.domain.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolNombre nombre);
}
