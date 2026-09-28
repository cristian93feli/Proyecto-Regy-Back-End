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

    /** Valida todo el archivo antes de iniciar y devuelve inmediatamente el identificador del trabajo. */
    public ImportacionProductosProgresoDTO iniciar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_VACIO);
        }
        try {
            byte[] contenido = archivo.getBytes();
            int total = archivoService.validar(contenido);
            String id = estadoStore.crear(total);
            asyncService.procesar(id, contenido);
            return estadoStore.obtener(id);
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    public ImportacionProductosProgresoDTO consultar(String id) {
        return estadoStore.obtener(id);
    }
}
