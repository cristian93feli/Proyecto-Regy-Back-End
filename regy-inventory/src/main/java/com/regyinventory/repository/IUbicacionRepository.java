package com.regyinventory.repository;

import com.regyinventory.entities.Ubicacion;
import com.regyinventory.enums.TipoUbicacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUbicacionRepository extends JpaRepository<Ubicacion, Long> {
    Optional<Ubicacion> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByUbicacionPadreId(Long ubicacionPadreId);

    List<Ubicacion> findByTipo(TipoUbicacion tipo);

    List<Ubicacion> findByUbicacionPadreId(Long ubicacionPadreId);

    /** Localiza las zonas de empaque asignadas como responsabilidad de un usuario. */
    List<Ubicacion> findDistinctByUsuariosResponsablesIdAndTipo(Long usuarioId, TipoUbicacion tipo);
}
