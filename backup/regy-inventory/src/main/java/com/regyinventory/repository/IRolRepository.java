package com.regyinventory.repository;

import com.regyinventory.entities.Rol;
import com.regyinventory.enums.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRolRepository extends JpaRepository<Rol, Long> {


    Optional<Rol> findByNombre(NombreRol nombre);
}