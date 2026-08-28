package com.regyinventory.repository;

import com.regyinventory.entities.SolicitudReposicion;
import com.regyinventory.enums.EstadoSolicitud;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISolicitudReposicionRepository extends JpaRepository<SolicitudReposicion, Long> {
    Page<SolicitudReposicion> findByEstado(EstadoSolicitud estado, Pageable pageable);

    Page<SolicitudReposicion> findByZonaDestinoIdIn(Set<Long> zonaDestinoIds, Pageable pageable);

    Page<SolicitudReposicion> findByEstadoAndZonaDestinoIdIn(
            EstadoSolicitud estado,
            Set<Long> zonaDestinoIds,
            Pageable pageable
    );

    long countByEstado(EstadoSolicitud estado);

    long countByEstadoAndZonaDestinoIdIn(EstadoSolicitud estado, Set<Long> zonaDestinoIds);

    boolean existsByProductoIdAndEstado(Long productoId, EstadoSolicitud estado);

    boolean existsByProductoId(Long productoId);

    boolean existsByZonaDestinoId(Long zonaDestinoId);
}
