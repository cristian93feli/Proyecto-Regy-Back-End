package com.regyinventory.service.implementation.importacionstock;

import java.time.LocalDate;

/** Entrada validada de Excel: identificadores en texto para conservar ceros iniciales. */
public record FilaImportacionStock(int fila, String numero, String sku, String codigoBarras,
        String codigoUbicacion, int cantidad, LocalDate vencimiento, String observaciones,
        Long productoId, Long ubicacionId) { }
