package com.regyinventory.repository;

import com.regyinventory.entities.LoteInventario;
import jakarta.persistence.LockModeType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ILoteInventarioRepository extends JpaRepository<LoteInventario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lote from LoteInventario lote where lote.producto.id = :productoId and lote.ubicacion.id = :ubicacionId and lote.cantidad > 0")
    List<LoteInventario> buscarLotesParaActualizar(
            @Param("productoId") Long productoId,
            @Param("ubicacionId") Long ubicacionId
    );

    @Query("select coalesce(sum(lote.cantidad), 0) from LoteInventario lote where lote.producto.id = :productoId")
    Integer stockTotalProducto(@Param("productoId") Long productoId);

    @Query("select coalesce(sum(lote.cantidad), 0) from LoteInventario lote where lote.producto.id = :productoId and lote.ubicacion.id = :ubicacionId")
    Integer stockUbicacion(@Param("productoId") Long productoId, @Param("ubicacionId") Long ubicacionId);

    boolean existsByProductoIdAndCantidadGreaterThan(Long productoId, Integer cantidad);

    boolean existsByUbicacionIdAndCantidadGreaterThan(Long ubicacionId, Integer cantidad);

    List<LoteInventario> findAllByCantidadGreaterThan(Integer cantidad);
}
