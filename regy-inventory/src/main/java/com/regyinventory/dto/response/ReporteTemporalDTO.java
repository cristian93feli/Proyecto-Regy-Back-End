package com.regyinventory.dto.response;

import java.time.Instant;

public record ReporteTemporalDTO(String id, String tipo, String archivoOrigen, String nombreReporte,
                                 Instant creadoEn, Instant venceEn, boolean descargado) { }
