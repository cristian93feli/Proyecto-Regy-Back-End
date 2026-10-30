package com.regyinventory.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Solo metadatos de un reporte Excel; nunca guarda datos de inventario ni el Excel en PostgreSQL. */
@Entity
@Table(name = "reportes_temporales", indexes = {
        @Index(name = "idx_reportes_usuario_vigencia", columnList = "usuario_id,vence_en"),
        @Index(name = "idx_reportes_vencimiento", columnList = "vence_en")
})
@Getter @Setter
public class ReporteTemporal {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "importacion_id", nullable = false, length = 36, unique = true)
    private String importacionId;
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    @Column(nullable = false, length = 16)
    private String tipo;
    @Column(name = "archivo_origen", length = 255)
    private String archivoOrigen;
    @Column(name = "nombre_reporte", nullable = false, length = 255)
    private String nombreReporte;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;
    @Column(name = "descargado_en")
    private Instant descargadoEn;
}
