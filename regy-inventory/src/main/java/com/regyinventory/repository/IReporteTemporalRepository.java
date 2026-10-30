package com.regyinventory.repository;

import com.regyinventory.entities.ReporteTemporal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IReporteTemporalRepository extends JpaRepository<ReporteTemporal, String> {
    List<ReporteTemporal> findByUsuarioIdAndVenceEnAfterOrderByCreadoEnDesc(Long usuarioId, Instant ahora);
    Optional<ReporteTemporal> findByIdAndUsuarioIdAndVenceEnAfter(String id, Long usuarioId, Instant ahora);
    List<ReporteTemporal> findByVenceEnLessThanEqual(Instant ahora);
}
