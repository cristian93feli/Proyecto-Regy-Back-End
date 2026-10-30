package com.regyinventory.service.implementation.importacion;

import com.regyinventory.dto.response.ErrorImportacionProductoDTO;
import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.utils.constants.importacion.ConstantesImportacionProducto;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.numeros.Numeros;
import com.regyinventory.utils.excel.ExcelCellValueReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductoImportacionArchivoService {

    private final ProductoImportacionFilaService filaService;
    private final ExcelCellValueReader cellValueReader;

    /**
     * Valida la estructura completa y separa las filas inválidas de las válidas.
     * Los errores de una fila no bloquean la importación de las demás.
     */
    public ValidacionImportacionProducto validar(byte[] contenido) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hoja = obtenerHoja(workbook);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> columnas = obtenerColumnasImportacion(
                    hoja.getRow(Numeros.CERO),
                    formatter,
                    evaluator
            );

            Map<Integer, DatosImportacionProducto> datosPorFila = new LinkedHashMap<>();
            Map<Integer, List<String>> erroresPorFila = new LinkedHashMap<>();
            Map<String, List<Integer>> numeros = new LinkedHashMap<>();
            Map<String, List<Integer>> codigos = new LinkedHashMap<>();
            Map<String, List<Integer>> skus = new LinkedHashMap<>();

            for (int indice = Numeros.UNO; indice <= hoja.getLastRowNum(); indice++) {
                Row fila = hoja.getRow(indice);
                if (fila == null || filaVacia(fila, formatter, evaluator)) continue;
                int numeroFila = indice + Numeros.UNO;
                if (datosPorFila.size() >= 50000) {
                    throw new BusinessException("El Excel supera el máximo de 50.000 filas de productos.");
                }
                DatosImportacionProducto datos = construirDatos(fila, formatter, evaluator, columnas);
                datosPorFila.put(numeroFila, datos);
                List<String> errores = new ArrayList<>();
                validarPrecisionIdentificadores(fila, numeroFila, columnas, errores);
                if (datos.numero() == null || datos.numero().isBlank()) {
                    errores.add("Número de Producto vacío.");
                } else {
                    registrarIdentificador(numeros, datos.numero(), numeroFila);
                }
                if (datos.nombre() == null || datos.nombre().isBlank()) errores.add("Nombre del Producto vacío.");
                registrarIdentificador(codigos, datos.codigoBarras(), numeroFila);
                registrarIdentificador(skus, datos.sku(), numeroFila);
                erroresPorFila.put(numeroFila, errores);
            }
            if (datosPorFila.isEmpty()) {
                throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_SIN_REGISTROS);
            }

            // Todas las filas implicadas se rechazan, incluida la primera aparición.
            marcarDuplicados(numeros, "Número de Producto", erroresPorFila);
            marcarDuplicados(codigos, "Código de Barras", erroresPorFila);
            marcarDuplicados(skus, "SKU", erroresPorFila);
            Set<Integer> invalidas = new LinkedHashSet<>();
            List<ErrorImportacionProductoDTO> errores = new ArrayList<>();
            erroresPorFila.forEach((numeroFila, motivos) -> {
                if (!motivos.isEmpty()) {
                    invalidas.add(numeroFila);
                    errores.add(construirError(numeroFila, datosPorFila.get(numeroFila), String.join(" | ", motivos)));
                }
            });
            return new ValidacionImportacionProducto(datosPorFila.size(), invalidas, errores);
        } catch (BusinessException excepcion) {
            throw excepcion;
        } catch (IOException | RuntimeException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    /** Procesa únicamente las filas válidas y conserva los rechazos de la validación previa. */
    public ImportacionProductosResponseDTO procesar(
            byte[] contenido,
            ValidacionImportacionProducto validacion,
            Consumer<ImportacionProductosResponseDTO> progreso
    ) {
        ImportacionProductosResponseDTO resultado = nuevoResultado(validacion);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hoja = obtenerHoja(workbook);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> columnas = obtenerColumnasImportacion(hoja.getRow(Numeros.CERO), formatter, evaluator);

            for (int indice = Numeros.UNO; indice <= hoja.getLastRowNum(); indice++) {
                Row fila = hoja.getRow(indice);
                if (fila == null || filaVacia(fila, formatter, evaluator)) {
                    continue;
                }

                int numeroFila = indice + Numeros.UNO;
                if (validacion.filasInvalidas().contains(numeroFila)) {
                    continue;
                }

                procesarFila(fila, numeroFila, formatter, evaluator, columnas, resultado);
                resultado.setFilasProcesadas(resultado.getFilasProcesadas() + Numeros.UNO);
                // Actualizar en bloques evita copiar grandes listas de errores por cada fila.
                if (resultado.getFilasProcesadas() % 20 == 0) progreso.accept(resultado);
            }

            adjuntarArchivoErrores(resultado);
            progreso.accept(resultado);
            return resultado;
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    private ImportacionProductosResponseDTO nuevoResultado(ValidacionImportacionProducto validacion) {
        return ImportacionProductosResponseDTO.builder()
                .filasProcesadas(validacion.filasInvalidas().size())
                .productosCreados(Numeros.CERO)
                .marcasCreadas(Numeros.CERO)
                .categoriasCreadas(Numeros.CERO)
                .errores(new ArrayList<>(validacion.errores()))
                .build();
    }

    private void procesarFila(
            Row fila,
            int numeroFila,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, Integer> columnas,
            ImportacionProductosResponseDTO resultado
    ) {
        DatosImportacionProducto datos = construirDatos(fila, formatter, evaluator, columnas);
        try {
            ResultadoImportacionFila resultadoFila = filaService.importarFila(datos);
            resultado.setProductosCreados(resultado.getProductosCreados() + Numeros.UNO);
            if (resultadoFila.marcaCreada()) {
                resultado.setMarcasCreadas(resultado.getMarcasCreadas() + Numeros.UNO);
            }
            if (resultadoFila.categoriaCreada()) {
                resultado.setCategoriasCreadas(resultado.getCategoriasCreadas() + Numeros.UNO);
            }
        } catch (RuntimeException excepcion) {
            resultado.getErrores().add(construirError(numeroFila, datos, obtenerCausaReal(excepcion)));
        }
    }

    private void registrarIdentificador(Map<String, List<Integer>> apariciones, String valor, int fila) {
        if (valor != null && !valor.isBlank()) {
            apariciones.computeIfAbsent(valor.trim().toUpperCase(Locale.ROOT), clave -> new ArrayList<>()).add(fila);
        }
    }

    private void marcarDuplicados(Map<String, List<Integer>> apariciones, String campo,
                                 Map<Integer, List<String>> erroresPorFila) {
        apariciones.forEach((valor, filas) -> {
            if (filas.size() > 1) {
                // Limitar el texto compartido por miles de filas evita consumo cuadrático de memoria.
                String referencias = filas.stream().limit(200).map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(", "));
                if (filas.size() > 200) referencias += " (y " + (filas.size() - 200) + " filas más)";
                String motivo = campo + " duplicado en el Excel (" + valor + "). Filas: " + referencias + ".";
                filas.forEach(fila -> erroresPorFila.get(fila).add(motivo));
            }
        });
    }

    private void validarPrecisionIdentificadores(
            Row fila,
            int numeroFila,
            Map<String, Integer> columnas,
            List<String> errores
    ) {
        validarPrecisionCelda(
                fila,
                columna(columnas, ConstantesImportacionProducto.ENCABEZADO_NUMERO),
                numeroFila,
                "Número de Producto",
                errores
        );
        validarPrecisionCelda(
                fila,
                columna(columnas, ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS),
                numeroFila,
                "Código de Barras",
                errores
        );
        validarPrecisionCelda(
                fila,
                columna(columnas, ConstantesImportacionProducto.ENCABEZADO_SKU),
                numeroFila,
                "SKU",
                errores
        );
    }

    /** Bloquea solo la fila cuando Excel pudo perder precisión de un identificador largo. */
    private void validarPrecisionCelda(
            Row fila,
            int columna,
            int numeroFila,
            String nombreCampo,
            List<String> errores
    ) {
        Cell celda = fila.getCell(columna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cellValueReader.exceedsSafeExcelIntegerPrecision(celda)) {
            errores.add(nombreCampo
                    + " supera 15 dígitos y está almacenado como número. Formatea esa columna como Texto para evitar pérdida de precisión.");
        }
    }

    private DatosImportacionProducto construirDatos(
            Row fila,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, Integer> columnas
    ) {
        return new DatosImportacionProducto(
                valorCelda(fila, columna(columnas, ConstantesImportacionProducto.ENCABEZADO_NUMERO), formatter, evaluator),
                valorCelda(fila, columna(columnas, ConstantesImportacionProducto.ENCABEZADO_MARCA), formatter, evaluator),
                valorCelda(fila, columna(columnas, ConstantesImportacionProducto.ENCABEZADO_CATEGORIA), formatter, evaluator),
                valorCelda(fila, columna(columnas, ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS), formatter, evaluator),
                valorCelda(fila, columna(columnas, ConstantesImportacionProducto.ENCABEZADO_NOMBRE), formatter, evaluator),
                valorCelda(fila, columna(columnas, ConstantesImportacionProducto.ENCABEZADO_SKU), formatter, evaluator)
        );
    }

    private ErrorImportacionProductoDTO construirError(
            int numeroFila,
            DatosImportacionProducto datos,
            String mensaje
    ) {
        return ErrorImportacionProductoDTO.builder()
                .fila(numeroFila)
                .numeroProducto(datos.numero())
                .marca(datos.marca())
                .categoria(datos.categoria())
                .codigoBarras(datos.codigoBarras())
                .nombreProducto(datos.nombre())
                .sku(datos.sku())
                .mensaje(mensaje)
                .build();
    }

    private Sheet obtenerHoja(Workbook workbook) {
        if (workbook.getNumberOfSheets() == Numeros.CERO) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
        return workbook.getSheetAt(Numeros.CERO);
    }

    private Map<String, Integer> obtenerColumnasImportacion(
            Row cabecera,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        if (cabecera == null) {
            throw new BusinessException(MensajesError.Producto.COLUMNAS_IMPORTACION_INVALIDAS);
        }

        Map<String, Integer> columnas = new HashMap<>();
        for (int indice = Numeros.CERO; indice < cabecera.getLastCellNum(); indice++) {
            String encabezado = normalizarEncabezado(valorCelda(cabecera, indice, formatter, evaluator));
            if (!encabezado.isBlank()) {
                columnas.put(encabezado, indice);
            }
        }

        for (String requerido : ConstantesImportacionProducto.ENCABEZADOS) {
            if (!columnas.containsKey(normalizarEncabezado(requerido))) {
                throw new BusinessException(MensajesError.Producto.COLUMNAS_IMPORTACION_INVALIDAS);
            }
        }
        return columnas;
    }

    private int columna(Map<String, Integer> columnas, String encabezado) {
        return columnas.get(normalizarEncabezado(encabezado));
    }

    private String normalizarEncabezado(String valor) {
        return com.regyinventory.utils.texto.TextoUtil.normalizarBusqueda(valor).replaceAll("\\s+", " ");
    }

    private boolean filaVacia(Row fila, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (int indice = Numeros.CERO; indice < fila.getLastCellNum(); indice++) {
            if (!valorCelda(fila, indice, formatter, evaluator).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String valorCelda(Row fila, int indice, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = fila.getCell(indice, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cellValueReader.read(cell, formatter, evaluator);
    }

    /** Nunca expone SQL, nombres de restricciones ni trazas técnicas al usuario final. */
    private String obtenerCausaReal(RuntimeException excepcion) {
        Throwable actual = excepcion;
        while (actual != null) {
            if (actual instanceof BusinessException negocio && negocio.getMessage() != null) {
                return negocio.getMessage();
            }
            actual = actual.getCause();
        }
        if (excepcion instanceof org.springframework.dao.DataIntegrityViolationException) {
            return "No se pudo registrar: número, SKU o código de barras ya existente. Verifica los identificadores.";
        }
        return "No fue posible importar esta fila. Verifica sus datos e inténtalo nuevamente.";
    }

    /** Reporte de resultados: una fila por producto idéntico rechazado, conservando filas y motivos. */
    private void adjuntarArchivoErrores(ImportacionProductosResponseDTO resultado) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet resumen = workbook.createSheet("Resumen");
            String[] etiquetas = {"Filas analizadas", "Productos creados", "Filas rechazadas",
                    "Incidencias", "Marcas creadas", "Categorías creadas", "Fecha del proceso"};
            int rechazadas = resultado.getErrores().size();
            int[] valores = {resultado.getFilasProcesadas(), resultado.getProductosCreados(), rechazadas,
                    (int) resultado.getErrores().stream().mapToLong(e -> e.getMensaje().split(" \\| ").length).sum(),
                    resultado.getMarcasCreadas(), resultado.getCategoriasCreadas()};
            for (int i = 0; i < etiquetas.length; i++) {
                Row fila = resumen.createRow(i);
                fila.createCell(0).setCellValue(etiquetas[i]);
                if (i < valores.length) fila.createCell(1).setCellValue(valores[i]);
                else fila.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            resumen.autoSizeColumn(0);
            resumen.setColumnWidth(1, 22 * 256);

            Sheet rechazados = workbook.createSheet(ConstantesImportacionProducto.HOJA_RECHAZADOS);
            String[] headers = {"Filas originales", ConstantesImportacionProducto.ENCABEZADO_NUMERO,
                    ConstantesImportacionProducto.ENCABEZADO_MARCA, ConstantesImportacionProducto.ENCABEZADO_CATEGORIA,
                    ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS,
                    ConstantesImportacionProducto.ENCABEZADO_NOMBRE, ConstantesImportacionProducto.ENCABEZADO_SKU,
                    ConstantesImportacionProducto.ENCABEZADO_MOTIVO};
            Row encabezado = rechazados.createRow(0);
            for (int i = 0; i < headers.length; i++) encabezado.createCell(i).setCellValue(headers[i]);

            record RechazoAgrupado(ErrorImportacionProductoDTO ejemplo, List<Integer> filas, Set<String> motivos) { }
            Map<List<String>, RechazoAgrupado> agrupados = new LinkedHashMap<>();
            for (ErrorImportacionProductoDTO error : resultado.getErrores()) {
                // No fusionar productos distintos aunque compartan erróneamente un identificador.
                List<String> clave = List.of(seguro(error.getNumeroProducto()), seguro(error.getMarca()),
                        seguro(error.getCategoria()), seguro(error.getCodigoBarras()),
                        seguro(error.getNombreProducto()), seguro(error.getSku()));
                RechazoAgrupado agrupado = agrupados.computeIfAbsent(clave,
                        k -> new RechazoAgrupado(error, new ArrayList<>(), new LinkedHashSet<>()));
                agrupado.filas().add(error.getFila());
                agrupado.motivos().add(error.getMensaje());
            }
            int indice = 1;
            for (RechazoAgrupado grupo : agrupados.values()) {
                ErrorImportacionProductoDTO error = grupo.ejemplo();
                Row row = rechazados.createRow(indice++);
                String filasTexto = grupo.filas().stream().limit(2000).map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(", "));
                if (grupo.filas().size() > 2000) filasTexto += " (y " + (grupo.filas().size() - 2000) + " más)";
                row.createCell(0).setCellValue(filasTexto);
                row.createCell(1).setCellValue(seguro(error.getNumeroProducto()));
                row.createCell(2).setCellValue(seguro(error.getMarca()));
                row.createCell(3).setCellValue(seguro(error.getCategoria()));
                row.createCell(4).setCellValue(seguro(error.getCodigoBarras()));
                row.createCell(5).setCellValue(seguro(error.getNombreProducto()));
                row.createCell(6).setCellValue(seguro(error.getSku()));
                row.createCell(7).setCellValue(limitarTextoExcel(String.join(" | ", grupo.motivos())));
            }
            for (int i = 0; i < headers.length; i++) rechazados.setColumnWidth(i, (i == 7 ? 90 : 25) * 256);
            workbook.write(output);
            resultado.setArchivoErroresNombre(ConstantesImportacionProducto.ARCHIVO_ERRORES_NOMBRE);
            resultado.setArchivoErroresBase64(Base64.getEncoder().encodeToString(output.toByteArray()));
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    /** Una celda de Excel admite como máximo 32.767 caracteres. */
    private String limitarTextoExcel(String valor) {
        return valor.length() <= 32767 ? valor : valor.substring(0, 32740) + "... (texto abreviado)";
    }

    private String seguro(String valor) {
        return valor == null ? "" : valor;
    }
}
