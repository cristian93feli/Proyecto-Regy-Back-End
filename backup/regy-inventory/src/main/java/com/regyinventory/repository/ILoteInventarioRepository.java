package com.regyinventory.repository;

import com.regyinventory.entities.LoteInventario;
import com.regyinventory.enums.TipoDestino;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.*;

public interface ILoteInventarioRepository extends JpaRepository<LoteInventario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteInventario l where l.producto.id=:productoId and l.tipoDestino=:tipo and ((:tipo=com.regyinventory.enums.TipoDestino.UBICACION and l.ubicacion.id=:destinoId) or (:tipo=com.regyinventory.enums.TipoDestino.ZONA_EMPAQUE and l.zonaEmpaque.id=:destinoId)) and l.cantidad>0")
    List<LoteInventario> buscarLotesParaActualizar(@Param("productoId") Long productoId, @Param("tipo") TipoDestino tipo, @Param("destinoId") Long destinoId);

    @Query("select coalesce(sum(l.cantidad),0) from LoteInventario l where l.producto.id=:productoId")
    Integer stockTotalProducto(@Param("productoId") Long productoId);

    @Query("select coalesce(sum(l.cantidad),0) from LoteInventario l where l.producto.id=:productoId and l.tipoDestino=:tipo and ((:tipo=com.regyinventory.enums.TipoDestino.UBICACION and l.ubicacion.id=:destinoId) or (:tipo=com.regyinventory.enums.TipoDestino.ZONA_EMPAQUE and l.zonaEmpaque.id=:destinoId))")
    Integer stockDestino(@Param("productoId") Long productoId, @Param("tipo") TipoDestino tipo, @Param("destinoId") Long destinoId);

    boolean existsByProductoIdAndCantidadGreaterThan(Long productoId, Integer cantidad);

    List<LoteInventario> findAllByCantidadGreaterThan(Integer cantidad);
}
