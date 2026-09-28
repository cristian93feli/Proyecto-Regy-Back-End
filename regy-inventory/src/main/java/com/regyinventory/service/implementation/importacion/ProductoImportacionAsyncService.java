package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoImportacionAsyncService {

    private final ProductoImportacionArchivoService archivoService;
    private final ImportacionProductosEstadoStore estadoStore;

    /** Procesa un Excel validado en segundo plano y publica el avance real fila por fila. */
    @Async("taskExecutor")
    public void procesar(String id, byte[] contenido) {
        try {
            ImportacionProductosResponseDTO resultado = archivoService.procesar(
                    contenido,
                    parcial -> estadoStore.actualizar(id, parcial)
            );
            estadoStore.completar(id, resultado);
        } catch (RuntimeException excepcion) {
            estadoStore.fallar(id, excepcion.getMessage() == null ? MensajesError.Producto.IMPORTACION_FALLIDA : excepcion.getMessage());
        }
    }
}
