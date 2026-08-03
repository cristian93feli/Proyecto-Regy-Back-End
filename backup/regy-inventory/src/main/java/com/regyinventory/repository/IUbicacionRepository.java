package com.regyinventory.repository;

import com.regyinventory.entities.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUbicacionRepository extends JpaRepository<Ubicacion, Long> {
    boolean existsByDepositoIdAndCodigoIgnoreCase(Long depositoId, String codigo);

    boolean existsByDepositoIdAndCodigoIgnoreCaseAndIdNot(Long depositoId, String codigo, Long id);

    java.util.List<Ubicacion> findByDepositoId(Long depositoId);
}
