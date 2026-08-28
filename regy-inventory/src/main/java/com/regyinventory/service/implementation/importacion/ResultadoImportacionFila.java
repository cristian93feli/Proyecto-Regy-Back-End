package com.regyinventory.service.implementation.importacion;

/** Resultado confirmado de una fila importada dentro de su propia transacción. */
public record ResultadoImportacionFila(
        boolean marcaCreada,
        boolean categoriaCreada
) {
}
