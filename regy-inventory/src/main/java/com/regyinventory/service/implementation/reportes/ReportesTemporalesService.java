package com.regyinventory.service.implementation.reportes;

import com.regyinventory.dto.response.ReporteTemporalDTO;
import com.regyinventory.entities.ReporteTemporal;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IReporteTemporalRepository;
import com.regyinventory.service.implementation.OperacionSupport;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Reportes de siete días: metadatos en PostgreSQL y archivos privados en volumen persistente. */
@Service
@RequiredArgsConstructor
public class ReportesTemporalesService {
    private static final Logger LOG = LoggerFactory.getLogger(ReportesTemporalesService.class);
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final IReporteTemporalRepository repository;
    private final OperacionSupport operacionSupport;

    @Value("${regy.reportes.directorio:./data/reportes-regy}")
    private String directorio;
    @Value("${regy.reportes.dias-retencion:7}")
    private long diasRetencion;

    /** Solo se llama cuando el proceso termina; no guarda el Excel de origen. */
    public String guardar(String importacionId, String tipo, String archivoOrigen, String nombreReporte, String base64) {
        if (base64 == null || base64.isBlank()) return null;
        if (!"PRODUCTOS".equals(tipo) && !"STOCK".equals(tipo)) throw new IllegalArgumentException("Tipo no permitido");
        Long usuarioId = operacionSupport.obtenerUsuarioAutenticado().getId();
        String id = UUID.randomUUID().toString();
        Path destino = ruta(id);
        Path temporal = null;
        try {
            Files.createDirectories(destino.getParent());
            temporal = Files.createTempFile(destino.getParent(), "reporte-", ".tmp");
            Files.write(temporal, Base64.getDecoder().decode(base64));
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino);
            }
            ReporteTemporal reporte = new ReporteTemporal();
            reporte.setId(id);
            reporte.setImportacionId(importacionId);
            reporte.setUsuarioId(usuarioId);
            reporte.setTipo(tipo);
            reporte.setArchivoOrigen(nombreSeguro(archivoOrigen, "importacion.xlsx"));
            reporte.setNombreReporte(nombreSeguro(nombreReporte, "resultado_importacion.xlsx"));
            reporte.setCreadoEn(Instant.now());
            reporte.setVenceEn(reporte.getCreadoEn().plus(Math.max(1, diasRetencion), ChronoUnit.DAYS));
            repository.save(reporte);
            return id;
        } catch (IOException | RuntimeException ex) {
            try { Files.deleteIfExists(destino); } catch (IOException ignored) { /* Se limpiará por mantenimiento. */ }
            LOG.error("No se pudo almacenar el reporte temporal de {}", tipo, ex);
            throw new BusinessException("Los datos se importaron, pero no se pudo conservar el reporte Excel.");
        } finally {
            if (temporal != null) try { Files.deleteIfExists(temporal); } catch (IOException ignored) { }
        }
    }

    public List<ReporteTemporalDTO> listar() {
        Long usuarioId = operacionSupport.obtenerUsuarioAutenticado().getId();
        return repository.findByUsuarioIdAndVenceEnAfterOrderByCreadoEnDesc(usuarioId, Instant.now())
                .stream().filter(r -> permitido(r.getTipo())).map(this::dto).toList();
    }

    /** Entrega el archivo solo al dueño con permiso vigente; descargado no equivale a eliminarlo. */
    public ArchivoReporte descargar(String id) {
        Long usuarioId = operacionSupport.obtenerUsuarioAutenticado().getId();
        ReporteTemporal reporte = repository.findByIdAndUsuarioIdAndVenceEnAfter(id, usuarioId, Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("El reporte no existe o ya venció."));
        if (!permitido(reporte.getTipo())) throw new ResourceNotFoundException("Reporte no disponible.");
        try {
            byte[] contenido = Files.readAllBytes(ruta(id));
            if (reporte.getDescargadoEn() == null) {
                reporte.setDescargadoEn(Instant.now());
                repository.save(reporte);
            }
            return new ArchivoReporte(reporte.getNombreReporte(), contenido, XLSX);
        } catch (IOException ex) {
            LOG.warn("Archivo de reporte no disponible: {}", id, ex);
            throw new ResourceNotFoundException("El archivo del reporte ya no está disponible.");
        }
    }

    /** Tarea idempotente: elimina los reportes expirados cada hora, sin tocar las importaciones realizadas. */
    @Scheduled(fixedDelayString = "${regy.reportes.limpieza-ms:3600000}")
    public void limpiarVencidos() {
        for (ReporteTemporal reporte : repository.findByVenceEnLessThanEqual(Instant.now())) {
            try {
                Files.deleteIfExists(ruta(reporte.getId()));
                repository.delete(reporte);
            } catch (IOException | RuntimeException ex) {
                LOG.warn("Se reintentará la eliminación del reporte {}", reporte.getId(), ex);
            }
        }
    }

    private ReporteTemporalDTO dto(ReporteTemporal reporte) {
        return new ReporteTemporalDTO(reporte.getId(), reporte.getTipo(), reporte.getArchivoOrigen(),
                reporte.getNombreReporte(), reporte.getCreadoEn(), reporte.getVenceEn(), reporte.getDescargadoEn() != null);
    }

    private boolean permitido(String tipo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        String permiso = "PRODUCTOS".equals(tipo) ? "PRODUCT_CREATE" : "STOCK_RECEIVE";
        return auth.getAuthorities().stream().anyMatch(a -> permiso.equals(a.getAuthority()));
    }

    private Path ruta(String id) {
        // La ruta proviene únicamente de un UUID generado en el servidor; jamás del nombre enviado por el cliente.
        return Path.of(directorio).toAbsolutePath().normalize().resolve(id + ".xlsx");
    }

    private String nombreSeguro(String valor, String defecto) {
        if (valor == null || valor.isBlank()) return defecto;
        String limpio = valor.replace('\\', '/');
        limpio = limpio.substring(limpio.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\\"]", "_");
        return limpio.length() > 255 ? limpio.substring(0, 255) : limpio;
    }

    public record ArchivoReporte(String nombre, byte[] contenido, String tipoMime) { }
}
