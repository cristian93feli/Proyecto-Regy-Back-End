package com.regyinventory.service.implementation.importacionstock;

import com.regyinventory.dto.response.ErrorImportacionStockDTO;
import com.regyinventory.dto.response.ImportacionStockProgresoDTO;
import com.regyinventory.enums.EstadoImportacionProducto;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.service.implementation.OperacionSupport;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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
public class ImportacionStockEstadoStore {
    private final OperacionSupport operacionSupport;
    private final ReportesTemporalesService reportes;
    private final Map<String, String> nombresArchivos = new ConcurrentHashMap<>();
    private final Map<String, ImportacionStockProgresoDTO> estados = new ConcurrentHashMap<>();
    private final Map<String, Long> propietarios = new ConcurrentHashMap<>();
    private final Map<String, Instant> finalizados = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> ubicacionesProcesadas = new ConcurrentHashMap<>();

    /** Impide importaciones simultáneas del mismo usuario y protege la consulta de otros usuarios. */
    public synchronized String crear(ValidacionImportacionStock validacion, String nombreArchivo) {
        Long usuarioId = operacionSupport.obtenerUsuarioAutenticado().getId();
        boolean activa = propietarios.entrySet().stream().anyMatch(entrada ->
                entrada.getValue().equals(usuarioId) && estados.containsKey(entrada.getKey())
                && estados.get(entrada.getKey()).getEstado() == EstadoImportacionProducto.PROCESANDO);
        if (activa) throw new BusinessException("Ya existe una importación de stock en proceso.");
        String id = UUID.randomUUID().toString();
        propietarios.put(id, usuarioId);
        nombresArchivos.put(id, nombreArchivo == null ? "stock.xlsx" : nombreArchivo);
        ubicacionesProcesadas.put(id, new HashSet<>());
        int rechazados = validacion.errores().size();
        estados.put(id, ImportacionStockProgresoDTO.builder()
                .id(id).estado(EstadoImportacionProducto.PROCESANDO)
                .total(validacion.total()).procesados(rechazados).exitosos(0).rechazados(rechazados)
                .erroresCantidad(rechazados).unidadesIngresadas(0).ubicacionesAfectadas(0)
                .porcentaje(validacion.total() == 0 ? 0 : rechazados * 100 / validacion.total())
                .mensaje("Importando stock...").errores(new ArrayList<>(validacion.errores())).build());
        return id;
    }

    public synchronized ImportacionStockProgresoDTO obtener(String id) {
        return copia(id, false);
    }

    /** Copia completa solo para generar el Excel, no para cada petición de progreso. */
    public synchronized ImportacionStockProgresoDTO obtenerCompleto(String id) {
        return copia(id, true);
    }

    private ImportacionStockProgresoDTO copia(String id, boolean completa) {
        ImportacionStockProgresoDTO estado = estadoInterno(id);
        return ImportacionStockProgresoDTO.builder()
                .id(estado.getId()).estado(estado.getEstado()).total(estado.getTotal())
                .procesados(estado.getProcesados()).exitosos(estado.getExitosos())
                .rechazados(estado.getRechazados()).erroresCantidad(estado.getErroresCantidad())
                .unidadesIngresadas(estado.getUnidadesIngresadas())
                .ubicacionesAfectadas(estado.getUbicacionesAfectadas())
                .porcentaje(estado.getPorcentaje()).mensaje(estado.getMensaje())
                .reporteId(estado.getReporteId())
                .archivoErroresNombre(estado.getArchivoErroresNombre())
                .archivoErroresBase64(null)
                .errores(new ArrayList<>(completa ? estado.getErrores()
                        : estado.getErrores().stream().limit(50).toList())).build();
    }

    private ImportacionStockProgresoDTO estadoInterno(String id) {
        if (!operacionSupport.obtenerUsuarioAutenticado().getId().equals(propietarios.get(id))) {
            throw new ResourceNotFoundException("No se encontró esta importación de stock.");
        }
        ImportacionStockProgresoDTO estado = estados.get(id);
        if (estado == null) throw new ResourceNotFoundException("No se encontró esta importación de stock.");
        return estado;
    }

    /** Cada ingreso confirmado se refleja en el estado consultable. */
    public synchronized void registrar(String id, int unidades, Long ubicacionId, ErrorImportacionStockDTO error) {
        ImportacionStockProgresoDTO estado = estadoInterno(id);
        estado.setProcesados(estado.getProcesados() + 1);
        if (error == null) {
            estado.setExitosos(estado.getExitosos() + 1);
            estado.setUnidadesIngresadas(estado.getUnidadesIngresadas() + unidades);
            ubicacionesProcesadas.get(id).add(ubicacionId);
            estado.setUbicacionesAfectadas(ubicacionesProcesadas.get(id).size());
        } else {
            estado.getErrores().add(error);
            estado.setRechazados(estado.getRechazados() + 1);
            estado.setErroresCantidad(estado.getErrores().size());
        }
        estado.setPorcentaje(Math.min(100, estado.getProcesados() * 100 / Math.max(1, estado.getTotal())));
    }

    public synchronized void completar(String id, String reporte) {
        ImportacionStockProgresoDTO estado = estadoInterno(id);
        estado.setArchivoErroresNombre("resultado_importacion_stock.xlsx");
        try {
            estado.setReporteId(reportes.guardar(id, "STOCK", nombresArchivos.remove(id),
                    estado.getArchivoErroresNombre(), reporte));
        } catch (BusinessException ex) {
            estado.setMensaje(ex.getMessage());
        }
        estado.setPorcentaje(100);
        estado.setEstado(EstadoImportacionProducto.COMPLETADA);
        finalizados.put(id, Instant.now());
        if (estado.getMensaje() == null || !estado.getMensaje().contains("reporte"))
            estado.setMensaje("Importación de stock finalizada.");
    }

    public synchronized void fallar(String id) {
        ImportacionStockProgresoDTO estado = estadoInterno(id);
        estado.setEstado(EstadoImportacionProducto.ERROR);
        finalizados.put(id, Instant.now());
        estado.setMensaje("La importación no pudo completarse. Consulta los ingresos ya confirmados antes de repetirla.");
        nombresArchivos.remove(id);
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
            ubicacionesProcesadas.remove(id);
            return true;
        });
    }
}
