package com.violeta.fia.repository;

import com.violeta.fia.domain.ControlTecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlTecnicoRepository extends JpaRepository<ControlTecnico, Long> {

    List<ControlTecnico> findAllByOrderByFechaDescIdDesc();

    List<ControlTecnico> findByEscuderiaIdOrderByFechaDescIdDesc(Long escuderiaId);
}
