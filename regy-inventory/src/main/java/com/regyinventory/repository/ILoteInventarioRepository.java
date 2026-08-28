package com.regyinventory.repository;

import com.regyinventory.entities.LoteInventario;
import com.regyinventory.entities.Producto;
import com.regyinventory.entities.Ubicacion;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Set;
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

    boolean existsByProductoId(Long productoId);

    boolean existsByUbicacionId(Long ubicacionId);

    boolean existsByUbicacionIdAndCantidadGreaterThan(Long ubicacionId, Integer cantidad);

    List<LoteInventario> findAllByCantidadGreaterThan(Integer cantidad);

    @Query("select distinct lote.ubicacion from LoteInventario lote where lote.producto.id = :productoId and lote.cantidad > 0 and lote.ubicacion.activo = true")
    List<Ubicacion> buscarUbicacionesConStock(@Param("productoId") Long productoId);


    /** Ordena los lotes positivos del producto por FEFO y después FIFO para sugerir ubicaciones de origen. */
    @Query("select lote from LoteInventario lote where lote.producto.id = :productoId and lote.cantidad > 0 and lote.ubicacion.activo = true order by case when lote.fechaVencimiento is null then 1 else 0 end, lote.fechaVencimiento asc, lote.fechaIngreso asc, lote.id asc")
    List<LoteInventario> buscarLotesConStockOrdenados(@Param("productoId") Long productoId);

    /** Devuelve los productos activos que realmente tienen existencias positivas. */
    @Query("select distinct lote.producto from LoteInventario lote where lote.cantidad > 0 and lote.producto.activo = true")
    List<Producto> buscarProductosConStock();

    /** Devuelve productos con stock únicamente dentro de las zonas asignadas al empaquetador. */
    @Query("select distinct lote.producto from LoteInventario lote where lote.cantidad > 0 and lote.producto.activo = true and lote.ubicacion.id in :ubicacionIds")
    List<Producto> buscarProductosConStockEnUbicaciones(@Param("ubicacionIds") Set<Long> ubicacionIds);

    /** Obtiene lotes positivos de un producto en cajas ordenables posteriormente por FEFO/FIFO. */
    @Query("select lote from LoteInventario lote where lote.producto.id = :productoId and lote.cantidad > 0 and lote.ubicacion.tipo = com.regyinventory.enums.TipoUbicacion.CAJA and lote.ubicacion.activo = true")
    List<LoteInventario> buscarLotesEnCajas(@Param("productoId") Long productoId);
}
