package com.regyinventory.service.implementation.importacionstock;

import com.regyinventory.dto.request.IngresoStockRequestDTO;
import com.regyinventory.dto.response.ErrorImportacionStockDTO;
import com.regyinventory.dto.response.ImportacionStockProgresoDTO;
import com.regyinventory.service.contracts.IInventarioService;
import com.regyinventory.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportacionStockAsyncService {
    private final IInventarioService inventarioService;
    private final ImportacionStockEstadoStore estados;
    private final ImportacionStockArchivoService archivoService;

    /** Cada fila utiliza ingresar(), con transacción, permisos, lotes y auditoría existentes. */
    @Async("taskExecutor")
    public void procesar(String id, ValidacionImportacionStock validacion) {
        try {
            for (FilaImportacionStock fila : validacion.validas()) {
                try {
                    IngresoStockRequestDTO solicitud = new IngresoStockRequestDTO();
                    solicitud.setProductoId(fila.productoId());
                    solicitud.setUbicacionDestinoId(fila.ubicacionId());
                    solicitud.setCantidad(fila.cantidad());
                    solicitud.setFechaVencimiento(fila.vencimiento());
                    solicitud.setObservaciones(fila.observaciones());
                    inventarioService.ingresar(solicitud);
                    estados.registrar(id, fila.cantidad(), fila.ubicacionId(), null);
                } catch (RuntimeException ex) {
                    ErrorImportacionStockDTO error = ErrorImportacionStockDTO.builder()
                            .fila(fila.fila()).numeroProducto(fila.numero())
                            .sku(fila.sku()).codigoBarras(fila.codigoBarras())
                            .codigoUbicacion(fila.codigoUbicacion())
                            .cantidad(String.valueOf(fila.cantidad()))
                            .fechaVencimiento(fila.vencimiento() == null ? "" : fila.vencimiento().toString())
                            .observaciones(fila.observaciones())
                            .mensaje(mensajeSeguro(ex))
                            .build();
                    estados.registrar(id, 0, null, error);
                }
            }
            ImportacionStockProgresoDTO estado = estados.obtenerCompleto(id);
            estados.completar(id, archivoService.reporte(estado));
        } catch (RuntimeException ex) {
            estados.fallar(id);
        }
    }

    /** Comunica únicamente reglas de negocio, nunca SQL ni mensajes internos del motor. */
    private String mensajeSeguro(RuntimeException excepcion) {
        Throwable causa = excepcion;
        while (causa != null) {
            if (causa instanceof BusinessException negocio && negocio.getMessage() != null) {
                return negocio.getMessage();
            }
            causa = causa.getCause();
        }
        return "No se pudo registrar el ingreso. Verifica el producto, la ubicación y los permisos.";
    }
}
