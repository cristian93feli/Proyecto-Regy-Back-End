package com.regyinventory.repository;

import com.regyinventory.entities.SolicitudReposicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISolicitudReposicionRepository extends JpaRepository<SolicitudReposicion, Long> {
    org.springframework.data.domain.Page<SolicitudReposicion> findByEstado(com.regyinventory.enums.EstadoSolicitud estado, org.springframework.data.domain.Pageable pageable);

    long countByEstado(com.regyinventory.enums.EstadoSolicitud estado);

    boolean existsByProductoIdAndEstado(Long productoId, com.regyinventory.enums.EstadoSolicitud estado);
}
