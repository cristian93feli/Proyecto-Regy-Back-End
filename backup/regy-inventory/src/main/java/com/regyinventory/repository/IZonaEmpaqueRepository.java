package com.regyinventory.repository;

import com.regyinventory.entities.ZonaEmpaque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IZonaEmpaqueRepository extends JpaRepository<ZonaEmpaque, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    java.util.Optional<ZonaEmpaque> findByUsuarioAsignadoUsername(String username);
}
