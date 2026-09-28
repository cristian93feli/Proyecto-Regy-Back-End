package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ImportacionProductosProgresoDTO;
import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.enums.EstadoImportacionProducto;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ImportacionProductosEstadoStore {

    private final Map<String, ImportacionProductosProgresoDTO> estados = new ConcurrentHashMap<>();

    /** Crea el estado inicial de una importación validada. */
    public String crear(int total) {
        String id = UUID.randomUUID().toString();
        estados.put(id, ImportacionProductosProgresoDTO.builder()
                .id(id)
                .estado(EstadoImportacionProducto.PROCESANDO)
                .total(total)
                .procesados(0)
                .exitosos(0)
                .erroresCantidad(0)
                .porcentaje(0)
                .marcasCreadas(0)
                .categoriasCreadas(0)
                .mensaje(MensajesExito.Producto.IMPORTACION_EN_PROCESO)
                .build());
        return id;
    }

    /** Actualiza contadores usando el resultado confirmado hasta la última fila terminada. */
    public void actualizar(String id, ImportacionProductosResponseDTO resultado) {
        ImportacionProductosProgresoDTO estado = obtener(id);
        estado.setProcesados(resultado.getFilasProcesadas());
        estado.setExitosos(resultado.getProductosCreados());
        estado.setErroresCantidad(resultado.getErrores().size());
        estado.setMarcasCreadas(resultado.getMarcasCreadas());
        estado.setCategoriasCreadas(resultado.getCategoriasCreadas());
        int total = Math.max(estado.getTotal(), 1);
        estado.setPorcentaje(Math.min(100, (resultado.getFilasProcesadas() * 100) / total));
    }

    /** Marca una importación como finalizada y conserva el reporte descargable. */
    public void completar(String id, ImportacionProductosResponseDTO resultado) {
        ImportacionProductosProgresoDTO estado = obtener(id);
        actualizar(id, resultado);
        estado.setEstado(EstadoImportacionProducto.COMPLETADA);
        estado.setPorcentaje(100);
        estado.setArchivoErroresNombre(resultado.getArchivoErroresNombre());
        estado.setArchivoErroresBase64(resultado.getArchivoErroresBase64());
        estado.setErrores(resultado.getErrores());
        estado.setMensaje(MensajesExito.Producto.IMPORTACION_FINALIZADA);
    }

    /** Marca una importación como fallida sin ocultar la causa funcional. */
    public void fallar(String id, String mensaje) {
        ImportacionProductosProgresoDTO estado = obtener(id);
        estado.setEstado(EstadoImportacionProducto.ERROR);
        estado.setMensaje(mensaje);
    }

    public ImportacionProductosProgresoDTO obtener(String id) {
        ImportacionProductosProgresoDTO estado = estados.get(id);
        if (estado == null) {
            throw new ResourceNotFoundException(String.format(MensajesError.Producto.IMPORTACION_NO_ENCONTRADA, id));
        }
        return estado;
    }
}
