package com.regyinventory.repository;

import com.regyinventory.entities.AuditoriaInventario;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuditoriaInventarioRepository extends JpaRepository<AuditoriaInventario, Long> {

    Page<AuditoriaInventario> findByDestinoIdIn(Set<Long> destinoIds, Pageable pageable);
}
