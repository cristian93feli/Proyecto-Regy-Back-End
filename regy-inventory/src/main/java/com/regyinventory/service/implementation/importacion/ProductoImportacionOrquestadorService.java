package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ImportacionProductosProgresoDTO;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductoImportacionOrquestadorService {

    private final ProductoImportacionArchivoService archivoService;
    private final ProductoImportacionAsyncService asyncService;
    private final ImportacionProductosEstadoStore estadoStore;

    /** Valida el archivo y deja bloqueadas solo las filas inválidas antes de iniciar el procesamiento. */
    public ImportacionProductosProgresoDTO iniciar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_VACIO);
        }
        try {
            byte[] contenido = archivo.getBytes();
            ValidacionImportacionProducto validacion = archivoService.validar(contenido);
            String id = estadoStore.crear(validacion);
            asyncService.procesar(id, contenido, validacion);
            return estadoStore.obtener(id);
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    public ImportacionProductosProgresoDTO consultar(String id) {
        return estadoStore.obtener(id);
    }
}
