package com.regyinventory.service.implementation.importacionstock;

import com.regyinventory.dto.response.ErrorImportacionStockDTO;
import com.regyinventory.dto.response.ImportacionStockProgresoDTO;
import com.regyinventory.entities.Producto;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.service.implementation.OperacionSupport;
import com.regyinventory.utils.excel.ExcelCellValueReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportacionStockArchivoService {
    private static final String[] CABECERAS = {"Número de Producto", "SKU", "Código de Barras",
            "Código de Ubicación", "Cantidad", "Fecha de Vencimiento", "Observaciones"};
    private final ExcelCellValueReader lector;
    private final IProductoRepository productos;
    private final IUbicacionRepository ubicaciones;
    private final OperacionSupport operacionSupport;

    /** Verifica todo el Excel antes de crear movimientos. Rechaza filas inválidas individualmente. */
    public ValidacionImportacionStock validar(byte[] contenido) {
        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hoja = libro.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = libro.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> columnas = columnas(hoja.getRow(0), formatter, evaluator);
            Map<Integer, List<String>> motivos = new LinkedHashMap<>();
            Map<Integer, String[]> originales = new LinkedHashMap<>();
            Map<Integer, FilaImportacionStock> candidatas = new LinkedHashMap<>();
            Map<String, List<Integer>> repetidas = new LinkedHashMap<>();
            Map<String, Producto> cacheProductos = new HashMap<>();
            Map<String, Ubicacion> cacheUbicaciones = new HashMap<>();
            for (int indice = 1; indice <= hoja.getLastRowNum(); indice++) {
                Row fila = hoja.getRow(indice);
                if (fila == null || vacia(fila, formatter, evaluator)) continue;
                if (originales.size() >= 50000) throw new BusinessException("El Excel supera el máximo de 50.000 filas.");
                int numeroFila = indice + 1;
                String[] valores = new String[CABECERAS.length];
                for (int c = 0; c < CABECERAS.length; c++) {
                    Integer col = columnas.get(normalizar(CABECERAS[c]));
                    valores[c] = col == null ? "" : valor(fila, col, formatter, evaluator);
                }
                originales.put(numeroFila, valores);
                List<String> errores = new ArrayList<>();
                motivos.put(numeroFila, errores);
                for (int c : new int[]{0, 1, 2, 3}) {
                    Integer col = columnas.get(normalizar(CABECERAS[c]));
                    if (col != null && lector.exceedsSafeExcelIntegerPrecision(fila.getCell(col))) {
                        errores.add(CABECERAS[c] + " tiene más de 15 dígitos numéricos. Utiliza formato Texto.");
                    }
                }
                String numero = valores[0].trim();
                if (numero.isBlank()) errores.add("Número de Producto obligatorio.");
                String ubicacion = valores[3].trim();
                if (ubicacion.isBlank()) errores.add("Código de Ubicación obligatorio.");
                Integer cantidad = null;
                try {
                    cantidad = new BigDecimal(valores[4].trim()).intValueExact();
                    if (cantidad <= 0) throw new NumberFormatException();
                } catch (NumberFormatException | ArithmeticException ex) {
                    errores.add("Cantidad debe ser un número entero positivo.");
                }
                LocalDate fecha = null;
                if (!valores[5].isBlank()) {
                    try {
                        Integer colFecha = columnas.get(normalizar(CABECERAS[5]));
                        Cell celda = colFecha == null ? null : fila.getCell(colFecha);
                        if (celda != null && celda.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                                && DateUtil.isCellDateFormatted(celda)) {
                            fecha = celda.getLocalDateTimeCellValue().toLocalDate();
                        } else {
                            fecha = fecha(valores[5]);
                        }
                    } catch (RuntimeException ex) {
                        errores.add("Fecha de Vencimiento inválida. Usa AAAA-MM-DD.");
                    }
                }
                if (valores[6].length() > 500) errores.add("Observaciones supera 500 caracteres.");
                Producto producto = null;
                Ubicacion destino = null;
                if (!numero.isBlank()) {
                    String claveProducto = numero.toUpperCase(Locale.ROOT);
                    if (!cacheProductos.containsKey(claveProducto)) {
                        cacheProductos.put(claveProducto, productos.findByNumeroIgnoreCase(numero).orElse(null));
                    }
                    producto = cacheProductos.get(claveProducto);
                    if (producto == null) errores.add("El Número de Producto no existe en el catálogo.");
                    else if (!Boolean.TRUE.equals(producto.getActivo())) errores.add("El producto está inactivo.");
                    else {
                        if (!valores[1].isBlank() && !valores[1].trim().equalsIgnoreCase(
                                producto.getSku() == null ? "" : producto.getSku())) {
                            errores.add("El SKU no corresponde al Número de Producto.");
                        }
                        if (!valores[2].isBlank() && !valores[2].trim().equalsIgnoreCase(
                                producto.getCodigoBarras() == null ? "" : producto.getCodigoBarras())) {
                            errores.add("El Código de Barras no corresponde al Número de Producto.");
                        }
                    }
                }
                if (!ubicacion.isBlank()) {
                    String claveUbicacion = ubicacion.toUpperCase(Locale.ROOT);
                    if (!cacheUbicaciones.containsKey(claveUbicacion)) {
                        cacheUbicaciones.put(claveUbicacion, ubicaciones.findByCodigoIgnoreCase(ubicacion).orElse(null));
                    }
                    destino = cacheUbicaciones.get(claveUbicacion);
                    if (destino == null) errores.add("El Código de Ubicación no existe.");
                    else if (!Boolean.TRUE.equals(destino.getActivo())) errores.add("La ubicación está inactiva.");
                    else if (destino.getTipo() != TipoUbicacion.CAJA && destino.getTipo() != TipoUbicacion.ZONA_EMPAQUE) {
                        errores.add("Solo se permite ingresar stock en cajas y zonas de empaque.");
                    } else {
                        try {
                            operacionSupport.validarUbicacionPermitidaParaEmpaquetador(destino.getId());
                        } catch (RuntimeException ex) {
                            errores.add("No tienes permiso para ingresar stock en esta ubicación.");
                        }
                    }
                }
                if (cantidad != null && producto != null && destino != null) {
                    candidatas.put(numeroFila, new FilaImportacionStock(numeroFila, numero,
                            valores[1], valores[2], ubicacion, cantidad, fecha, valores[6],
                            producto.getId(), destino.getId()));
                    String clave = String.join("\u001f", numero.toUpperCase(Locale.ROOT),
                            ubicacion.toUpperCase(Locale.ROOT), String.valueOf(cantidad),
                            fecha == null ? "" : fecha.toString(), valores[6]);
                    repetidas.computeIfAbsent(clave, k -> new ArrayList<>()).add(numeroFila);
                }
            }
            if (originales.isEmpty()) throw new BusinessException("El Excel no contiene registros de stock.");
            repetidas.values().stream().filter(filas -> filas.size() > 1).forEach(filas -> {
                String mensaje = "Ingreso duplicado idéntico en las filas "
                        + filas.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ".";
                filas.forEach(f -> motivos.get(f).add(mensaje));
            });
            List<ErrorImportacionStockDTO> errores = new ArrayList<>();
            List<FilaImportacionStock> validas = new ArrayList<>();
            for (Map.Entry<Integer, String[]> entry : originales.entrySet()) {
                int fila = entry.getKey();
                if (motivos.get(fila).isEmpty()) validas.add(candidatas.get(fila));
                else errores.add(error(fila, entry.getValue(), String.join(" | ", motivos.get(fila))));
            }
            return new ValidacionImportacionStock(originales.size(), validas, errores);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException | IOException ex) {
            throw new BusinessException("El archivo Excel de stock no es válido. Utiliza la plantilla .xlsx.");
        }
    }

    private Map<String, Integer> columnas(Row encabezado, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (encabezado == null) throw new BusinessException("El Excel no contiene encabezados.");
        Map<String, Integer> resultado = new HashMap<>();
        for (int i = 0; i < encabezado.getLastCellNum(); i++) {
            String clave = normalizar(valor(encabezado, i, formatter, evaluator));
            if (!clave.isBlank() && resultado.putIfAbsent(clave, i) != null) {
                throw new BusinessException("La cabecera '" + clave + "' está repetida en el Excel.");
            }
        }
        for (String obligatorio : new String[]{CABECERAS[0], CABECERAS[3], CABECERAS[4]}) {
            if (!resultado.containsKey(normalizar(obligatorio))) {
                throw new BusinessException("Falta la columna obligatoria: " + obligatorio + ".");
            }
        }
        return resultado;
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replaceAll("\\s+", " ");
    }

    private String valor(Row fila, int indice, DataFormatter formatter, FormulaEvaluator evaluator) {
        return lector.read(fila.getCell(indice, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL), formatter, evaluator);
    }

    private boolean vacia(Row fila, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (int i = 0; i < fila.getLastCellNum(); i++) {
            if (!valor(fila, i, formatter, evaluator).isBlank()) return false;
        }
        return true;
    }

    private LocalDate fecha(String valor) {
        try { return LocalDate.parse(valor.trim()); }
        catch (DateTimeParseException ex) {
            return LocalDate.parse(valor.trim(), DateTimeFormatter.ofPattern("d/M/uuuu"));
        }
    }

    public ErrorImportacionStockDTO error(int fila, String[] datos, String mensaje) {
        return ErrorImportacionStockDTO.builder().fila(fila).numeroProducto(datos[0])
                .sku(datos[1]).codigoBarras(datos[2]).codigoUbicacion(datos[3])
                .cantidad(datos[4]).fechaVencimiento(datos[5]).observaciones(datos[6])
                .mensaje(mensaje).build();
    }

    /** Plantilla independiente: la importación de stock no crea productos ni catálogos. */
    public byte[] plantilla() {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Ingreso stock");
            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < CABECERAS.length; i++) {
                cabecera.createCell(i).setCellValue(CABECERAS[i]);
                hoja.setColumnWidth(i, (i == 6 ? 40 : 24) * 256);
            }
            org.apache.poi.ss.usermodel.CellStyle estilo = estiloTexto(libro);
            for (int fila = 1; fila <= 50; fila++) {
                Row row = hoja.createRow(fila);
                for (int col = 0; col < CABECERAS.length; col++) {
                    Cell cell = row.createCell(col);
                    cell.setCellStyle(estilo);
                }
            }
            libro.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException("No se pudo generar la plantilla de stock.");
        }
    }

    private org.apache.poi.ss.usermodel.CellStyle estiloTexto(Workbook libro) {
        org.apache.poi.ss.usermodel.CellStyle estilo = libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat("@"));
        return estilo;
    }

    private String seguro(String valor) { return valor == null ? "" : valor; }

    /** Excel final con unidades registradas, incidencias y cada fila rechazada. */
    public String reporte(ImportacionStockProgresoDTO estado) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet resumen = libro.createSheet("Resumen");
            String[] nombres = {"Filas analizadas", "Ingresos exitosos", "Filas rechazadas",
                    "Unidades ingresadas", "Ubicaciones afectadas", "Fecha del proceso"};
            String[] valores = {String.valueOf(estado.getTotal()), String.valueOf(estado.getExitosos()),
                    String.valueOf(estado.getRechazados()), String.valueOf(estado.getUnidadesIngresadas()),
                    String.valueOf(estado.getUbicacionesAfectadas()), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))};
            for (int i = 0; i < nombres.length; i++) {
                Row row = resumen.createRow(i);
                row.createCell(0).setCellValue(nombres[i]);
                row.createCell(1).setCellValue(valores[i]);
            }
            resumen.setColumnWidth(0, 30 * 256);
            resumen.setColumnWidth(1, 30 * 256);
            Sheet rechazados = libro.createSheet("Rechazados");
            String[] headers = {"Fila", "Número de Producto", "SKU", "Código de Barras",
                    "Código de Ubicación", "Cantidad", "Fecha de Vencimiento", "Observaciones", "Motivo"};
            Row cabecera = rechazados.createRow(0);
            for (int i = 0; i < headers.length; i++) cabecera.createCell(i).setCellValue(headers[i]);
            for (int i = 0; i < estado.getErrores().size(); i++) {
                ErrorImportacionStockDTO error = estado.getErrores().get(i);
                Row row = rechazados.createRow(i + 1);
                row.createCell(0).setCellValue(error.getFila());
                row.createCell(1).setCellValue(error.getNumeroProducto());
                row.createCell(2).setCellValue(seguro(error.getSku()));
                row.createCell(3).setCellValue(seguro(error.getCodigoBarras()));
                row.createCell(4).setCellValue(seguro(error.getCodigoUbicacion()));
                row.createCell(5).setCellValue(seguro(error.getCantidad()));
                row.createCell(6).setCellValue(seguro(error.getFechaVencimiento()));
                row.createCell(7).setCellValue(seguro(error.getObservaciones()));
                row.createCell(8).setCellValue(seguro(error.getMensaje()));
            }
            for (int i = 0; i < headers.length; i++) rechazados.setColumnWidth(i, (i == 8 ? 95 : 26) * 256);
            libro.write(output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException("No se pudo generar el reporte de stock.");
        }
    }
}
