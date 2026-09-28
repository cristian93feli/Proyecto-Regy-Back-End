package com.regyinventory.repository;

import com.regyinventory.entities.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface IMarcaRepository
        extends JpaRepository<Marca, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );

    java.util.Optional<Marca> findByNombreIgnoreCase(String nombre);

    /** Busca una marca usando una clave normalizada sin tildes, espacios repetidos ni diferencias de mayúsculas. */
    @Query(value = """
            select * from marcas marca
            where translate(regexp_replace(trim(lower(marca.nombre)), '[[:space:]]+', ' ', 'g'), 'áéíóúüñ', 'aeiouun') = :nombreNormalizado
            limit 1
            """, nativeQuery = true)
    java.util.Optional<Marca> findByNombreNormalizado(@Param("nombreNormalizado") String nombreNormalizado);

    /** Valida duplicidad normalizada excluyendo el registro actual. */
    @Query(value = """
            select exists(
                select 1 from marcas marca
                where marca.id <> :id
                  and translate(regexp_replace(trim(lower(marca.nombre)), '[[:space:]]+', ' ', 'g'), 'áéíóúüñ', 'aeiouun') = :nombreNormalizado
            )
            """, nativeQuery = true)
    boolean existsByNombreNormalizadoAndIdNot(@Param("nombreNormalizado") String nombreNormalizado, @Param("id") Long id);
}