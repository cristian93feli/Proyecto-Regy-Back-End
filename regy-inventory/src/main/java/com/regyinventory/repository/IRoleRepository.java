package com.regyinventory.repository;

import com.regyinventory.entities.Role;
import com.regyinventory.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNombre(RoleName nombre);
}