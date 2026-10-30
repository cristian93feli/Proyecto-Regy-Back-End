package com.regyinventory.service.implementation.importacionstock;

import com.regyinventory.dto.response.ImportacionStockProgresoDTO;
import com.regyinventory.exceptions.BusinessException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImportacionStockOrquestadorService {
    private final ImportacionStockArchivoService archivoService;
    private final ImportacionStockEstadoStore estados;
    private final ImportacionStockAsyncService async;

    /** Prevalida el archivo y retorna un ID que permite navegar sin interrumpir el trabajo. */
    public ImportacionStockProgresoDTO iniciar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new BusinessException("Selecciona un Excel de stock.");
        if (archivo.getSize() > 15 * 1024 * 1024) {
            throw new BusinessException("El archivo no puede superar 15 MB.");
        }
        try {
            ValidacionImportacionStock validacion = archivoService.validar(archivo.getBytes());
            String id = estados.crear(validacion, archivo.getOriginalFilename());
            async.procesar(id, validacion);
            return estados.obtener(id);
        } catch (IOException ex) {
            throw new BusinessException("No se pudo leer el archivo Excel de stock.");
        }
    }

    public ImportacionStockProgresoDTO consultar(String id) { return estados.obtener(id); }
    public byte[] plantilla() { return archivoService.plantilla(); }
}
