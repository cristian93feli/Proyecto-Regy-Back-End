package com.regyinventory.repository;

import com.regyinventory.entities.Permiso;
import com.regyinventory.enums.NombrePermiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByNombre(NombrePermiso nombre);

}