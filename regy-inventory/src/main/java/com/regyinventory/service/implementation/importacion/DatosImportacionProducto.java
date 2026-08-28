package com.regyinventory.service.implementation.importacion;

/** Datos normalizados de una fila del archivo de importación de productos. */
public record DatosImportacionProducto(
        String numero,
        String marca,
        String categoria,
        String codigoBarras,
        String nombre,
        String sku
) {
}
