package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ImportacionProductosProgresoDTO;
import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.enums.EstadoImportacionProducto;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.service.implementation.OperacionSupport;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import com.regyinventory.service.implementation.reportes.ReportesTemporalesService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportacionProductosEstadoStore {
    private final OperacionSupport operacionSupport;
    private final ReportesTemporalesService reportes;
    private final Map<String, String> nombresArchivos = new ConcurrentHashMap<>();
    private final Map<String, Long> propietarios = new ConcurrentHashMap<>();
    private final Map<String, Instant> finalizados = new ConcurrentHashMap<>();
    private final Map<String, ImportacionProductosProgresoDTO> estados = new ConcurrentHashMap<>();

    /** Bloquea una segunda importación del mismo usuario hasta que termine la primera. */
    public synchronized String crear(ValidacionImportacionProducto validacion, String nombreArchivo) {
        Long usuarioId = operacionSupport.obtenerUsuarioAutenticado().getId();
        boolean activa = propietarios.entrySet().stream().anyMatch(entrada ->
                entrada.getValue().equals(usuarioId)
                && estados.containsKey(entrada.getKey())
                && estados.get(entrada.getKey()).getEstado() == EstadoImportacionProducto.PROCESANDO);
        if (activa) throw new BusinessException("Ya existe una importación de productos en proceso. Espera a que finalice.");
        String id = UUID.randomUUID().toString();
        int total = validacion.total();
        int rechazadas = validacion.filasInvalidas().size();
        propietarios.put(id, usuarioId);
        nombresArchivos.put(id, nombreArchivo == null ? "productos.xlsx" : nombreArchivo);
        estados.put(id, ImportacionProductosProgresoDTO.builder()
                .id(id).estado(EstadoImportacionProducto.PROCESANDO)
                .total(total).procesados(rechazadas).exitosos(0).erroresCantidad(rechazadas)
                .porcentaje(total == 0 ? 0 : Math.min(100, (rechazadas * 100) / total))
                .marcasCreadas(0).categoriasCreadas(0)
                .errores(new ArrayList<>(validacion.errores().stream().limit(50).toList()))
                .mensaje(MensajesExito.Producto.IMPORTACION_EN_PROCESO).build());
        return id;
    }

    /** Publica los contadores de una fila confirmada sin exponer colecciones en mutación. */
    public synchronized void actualizar(String id, ImportacionProductosResponseDTO resultado) {
        ImportacionProductosProgresoDTO estado = estadoInterno(id);
        estado.setProcesados(resultado.getFilasProcesadas());
        estado.setExitosos(resultado.getProductosCreados());
        estado.setErroresCantidad(resultado.getErrores().size());
        estado.setMarcasCreadas(resultado.getMarcasCreadas());
        estado.setCategoriasCreadas(resultado.getCategoriasCreadas());
        estado.setErrores(new ArrayList<>(resultado.getErrores().stream().limit(50).toList()));
        estado.setPorcentaje(Math.min(100, (resultado.getFilasProcesadas() * 100) / Math.max(estado.getTotal(), 1)));
    }

    public synchronized void completar(String id, ImportacionProductosResponseDTO resultado) {
        actualizar(id, resultado);
        ImportacionProductosProgresoDTO estado = estadoInterno(id);
        estado.setArchivoErroresNombre(resultado.getArchivoErroresNombre());
        try {
            estado.setReporteId(reportes.guardar(id, "PRODUCTOS", nombresArchivos.remove(id),
                    resultado.getArchivoErroresNombre(), resultado.getArchivoErroresBase64()));
        } catch (BusinessException ex) {
            estado.setMensaje(ex.getMessage());
        }
        if (estado.getMensaje() == null || !estado.getMensaje().contains("reporte"))
            estado.setMensaje(MensajesExito.Producto.IMPORTACION_FINALIZADA);
        estado.setPorcentaje(100);
        estado.setEstado(EstadoImportacionProducto.COMPLETADA);
        finalizados.put(id, Instant.now());
    }

    public synchronized void fallar(String id, String mensaje) {
        ImportacionProductosProgresoDTO estado = estadoInterno(id);
        estado.setEstado(EstadoImportacionProducto.ERROR);
        finalizados.put(id, Instant.now());
        estado.setMensaje(mensaje);
        nombresArchivos.remove(id);
    }

    /** Copia inmutable por petición: la serialización HTTP no itera sobre una lista que se modifica. */
    public synchronized ImportacionProductosProgresoDTO obtener(String id) {
        ImportacionProductosProgresoDTO estado = estadoInterno(id);
        return ImportacionProductosProgresoDTO.builder()
                .id(estado.getId()).estado(estado.getEstado()).total(estado.getTotal())
                .procesados(estado.getProcesados()).exitosos(estado.getExitosos())
                .erroresCantidad(estado.getErroresCantidad()).porcentaje(estado.getPorcentaje())
                .marcasCreadas(estado.getMarcasCreadas()).categoriasCreadas(estado.getCategoriasCreadas())
                .reporteId(estado.getReporteId())
                .archivoErroresNombre(estado.getArchivoErroresNombre())
                .archivoErroresBase64(null)
                .mensaje(estado.getMensaje()).errores(new ArrayList<>(estado.getErrores())).build();
    }

    private ImportacionProductosProgresoDTO estadoInterno(String id) {
        Long usuarioId = operacionSupport.obtenerUsuarioAutenticado().getId();
        if (!usuarioId.equals(propietarios.get(id))) {
            throw new ResourceNotFoundException(String.format(MensajesError.Producto.IMPORTACION_NO_ENCONTRADA, id));
        }
        ImportacionProductosProgresoDTO estado = estados.get(id);
        if (estado == null) {
            throw new ResourceNotFoundException(String.format(MensajesError.Producto.IMPORTACION_NO_ENCONTRADA, id));
        }
        return estado;
    }

    /** El seguimiento temporal vence a las 24 horas; los Excel persisten siete días por separado. */
    @Scheduled(fixedDelay = 3600000)
    public synchronized void limpiarEstadosFinalizados() {
        Instant limite = Instant.now().minus(1, ChronoUnit.DAYS);
        finalizados.entrySet().removeIf(item -> {
            if (item.getValue().isAfter(limite)) return false;
            String id = item.getKey();
            estados.remove(id);
            propietarios.remove(id);
            nombresArchivos.remove(id);
            return true;
        });
    }
}
