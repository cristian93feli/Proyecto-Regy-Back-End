package com.regyinventory.repository;

import com.regyinventory.entities.AuditoriaInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuditoriaInventarioRepository extends JpaRepository<AuditoriaInventario,Long>  {
}
