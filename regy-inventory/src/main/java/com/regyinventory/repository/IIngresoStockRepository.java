package com.regyinventory.repository;

import com.regyinventory.entities.IngresoStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IIngresoStockRepository extends JpaRepository<IngresoStock, Long> {
    boolean existsByProductoId(Long productoId);

    boolean existsByUbicacionId(Long ubicacionId);
}
