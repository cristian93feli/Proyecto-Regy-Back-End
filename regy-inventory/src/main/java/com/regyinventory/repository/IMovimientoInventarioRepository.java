package com.regyinventory.repository;

import com.regyinventory.entities.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    boolean existsByProductoId(Long productoId);

    boolean existsByUbicacionOrigenIdOrUbicacionDestinoId(Long ubicacionOrigenId, Long ubicacionDestinoId);
}
