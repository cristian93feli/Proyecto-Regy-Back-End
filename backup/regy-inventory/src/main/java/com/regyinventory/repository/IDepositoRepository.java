package com.regyinventory.repository;

import com.regyinventory.entities.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDepositoRepository extends JpaRepository<Deposito, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
