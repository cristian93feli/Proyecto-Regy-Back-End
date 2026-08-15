package com.regyinventory.repository;

import com.regyinventory.entities.Producto;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByNumeroIgnoreCase(String numero);

    boolean existsByNumeroIgnoreCaseAndIdNot(String numero, Long id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsByCodigoBarras(String codigo);

    boolean existsByCodigoBarrasAndIdNot(String codigo, Long id);

    /**
     * Busca productos activos por número, nombre o código de barras sin distinguir
     * mayúsculas ni tildes. Se usa SQL nativo para que PostgreSQL resuelva
     * {@code translate} como texto y evitar la incompatibilidad semántica de
     * Hibernate 7.4 con {@code function('translate', ...)} dentro de expresiones LIKE.
     */
    @Query(value = """
            select producto.*
            from productos producto
            where producto.activo = true
              and (
                    translate(lower(producto.numero), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
                 or translate(lower(producto.nombre), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
                 or translate(lower(coalesce(producto.codigo_barras, '')), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
              )
            order by producto.nombre asc
            """, nativeQuery = true)
    List<Producto> buscarActivos(@Param("termino") String termino, Pageable pageable);

    /** Lista únicamente productos con stock dentro de las zonas permitidas del empaquetador. */
    @Query("""
            select distinct lote.producto
            from LoteInventario lote
            where lote.cantidad > 0
              and lote.producto.activo = true
              and lote.ubicacion.id in :ubicacionIds
            """)
    Page<Producto> listarVisiblesEnUbicaciones(
            @Param("ubicacionIds") Set<Long> ubicacionIds,
            Pageable pageable
    );
}
