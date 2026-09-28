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
    /** Lista productos paginados con búsqueda global y filtros, ordenando el número de forma numérica ascendente. */
    @Query(value = """
            select producto.*
            from productos producto
            where (
                    :termino = ''
                    or translate(lower(producto.nombre), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
                    or lower(coalesce(producto.sku, '')) like concat('%', lower(:termino), '%')
                    or lower(coalesce(producto.codigo_barras, '')) like concat('%', lower(:termino), '%')
                  )
              and (cast(:marcaId as bigint) is null or producto.marca_id = :marcaId)
              and (cast(:categoriaId as bigint) is null or producto.categoria_id = :categoriaId)
              and (cast(:activo as boolean) is null or producto.activo = :activo)
            order by
              case when regexp_replace(producto.numero, '[^0-9]', '', 'g') = '' then 1 else 0 end asc,
              nullif(regexp_replace(producto.numero, '[^0-9]', '', 'g'), '')::numeric asc,
              lower(producto.numero) asc
            """,
            countQuery = """
            select count(*)
            from productos producto
            where (
                    :termino = ''
                    or translate(lower(producto.nombre), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
                    or lower(coalesce(producto.sku, '')) like concat('%', lower(:termino), '%')
                    or lower(coalesce(producto.codigo_barras, '')) like concat('%', lower(:termino), '%')
                  )
              and (cast(:marcaId as bigint) is null or producto.marca_id = :marcaId)
              and (cast(:categoriaId as bigint) is null or producto.categoria_id = :categoriaId)
              and (cast(:activo as boolean) is null or producto.activo = :activo)
            """, nativeQuery = true)
    Page<Producto> filtrarPaginado(
            @Param("termino") String termino,
            @Param("marcaId") Long marcaId,
            @Param("categoriaId") Long categoriaId,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    /** Aplica los mismos filtros limitando resultados a las zonas permitidas del empaquetador. */
    @Query(value = """
            select producto.*
            from productos producto
            where exists (
                    select 1 from lotes_inventario lote
                    where lote.producto_id = producto.id
                      and lote.cantidad > 0
                      and lote.ubicacion_id in (:ubicacionIds)
                  )
              and (
                    :termino = ''
                    or translate(lower(producto.nombre), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
                    or lower(coalesce(producto.sku, '')) like concat('%', lower(:termino), '%')
                    or lower(coalesce(producto.codigo_barras, '')) like concat('%', lower(:termino), '%')
                  )
              and (cast(:marcaId as bigint) is null or producto.marca_id = :marcaId)
              and (cast(:categoriaId as bigint) is null or producto.categoria_id = :categoriaId)
              and (cast(:activo as boolean) is null or producto.activo = :activo)
            order by
              case when regexp_replace(producto.numero, '[^0-9]', '', 'g') = '' then 1 else 0 end asc,
              nullif(regexp_replace(producto.numero, '[^0-9]', '', 'g'), '')::numeric asc,
              lower(producto.numero) asc
            """,
            countQuery = """
            select count(*)
            from productos producto
            where exists (
                    select 1 from lotes_inventario lote
                    where lote.producto_id = producto.id
                      and lote.cantidad > 0
                      and lote.ubicacion_id in (:ubicacionIds)
                  )
              and (
                    :termino = ''
                    or translate(lower(producto.nombre), 'áéíóúüñ', 'aeiouun') like concat('%', :termino, '%')
                    or lower(coalesce(producto.sku, '')) like concat('%', lower(:termino), '%')
                    or lower(coalesce(producto.codigo_barras, '')) like concat('%', lower(:termino), '%')
                  )
              and (cast(:marcaId as bigint) is null or producto.marca_id = :marcaId)
              and (cast(:categoriaId as bigint) is null or producto.categoria_id = :categoriaId)
              and (cast(:activo as boolean) is null or producto.activo = :activo)
            """, nativeQuery = true)
    Page<Producto> filtrarPaginadoVisibles(
            @Param("termino") String termino,
            @Param("marcaId") Long marcaId,
            @Param("categoriaId") Long categoriaId,
            @Param("activo") Boolean activo,
            @Param("ubicacionIds") Set<Long> ubicacionIds,
            Pageable pageable
    );

}
