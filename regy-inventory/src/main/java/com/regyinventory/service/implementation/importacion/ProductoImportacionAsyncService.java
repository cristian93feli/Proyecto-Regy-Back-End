package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductoImportacionAsyncService {

    private final ProductoImportacionArchivoService archivoService;
    private final ImportacionProductosEstadoStore estadoStore;

    /** Procesa en segundo plano solo las filas válidas y conserva los rechazos del preanálisis. */
    @Async("taskExecutor")
    public void procesar(String id, byte[] contenido, ValidacionImportacionProducto validacion) {
        try {
            ImportacionProductosResponseDTO resultado = archivoService.procesar(
                    contenido,
                    validacion,
                    parcial -> estadoStore.actualizar(id, parcial)
            );
            estadoStore.completar(id, resultado);
        } catch (RuntimeException excepcion) {
            estadoStore.fallar(
                    id,
                    excepcion.getMessage() == null ? MensajesError.Producto.IMPORTACION_FALLIDA : excepcion.getMessage()
            );
        }
    }
}
