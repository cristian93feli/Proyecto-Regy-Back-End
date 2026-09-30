package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ErrorImportacionProductoDTO;
import java.util.List;
import java.util.Set;

/** Resultado de la validación previa: conserva las filas rechazadas sin bloquear las filas válidas. */
public record ValidacionImportacionProducto(
        int total,
        Set<Integer> filasInvalidas,
        List<ErrorImportacionProductoDTO> errores
) {
}
