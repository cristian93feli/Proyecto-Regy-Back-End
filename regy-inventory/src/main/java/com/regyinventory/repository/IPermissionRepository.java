package com.regyinventory.repository;

import com.regyinventory.entities.Permission;
import com.regyinventory.enums.PermissionName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByNombre(PermissionName nombre);

}