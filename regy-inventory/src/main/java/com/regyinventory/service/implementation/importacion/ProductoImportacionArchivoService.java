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
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    /** Valida estructura, cabeceras y precisión de identificadores antes de iniciar la importación. */
    public int validar(byte[] contenido) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hoja = obtenerHoja(workbook);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> columnas = obtenerColumnasImportacion(hoja.getRow(Numeros.CERO), formatter, evaluator);
            int total = 0;
            List<String> errores = new ArrayList<>();
            java.util.Set<String> numeros = new java.util.HashSet<>();
            java.util.Set<String> codigos = new java.util.HashSet<>();
            java.util.Set<String> skus = new java.util.HashSet<>();

            for (int indice = Numeros.UNO; indice <= hoja.getLastRowNum(); indice++) {
                Row fila = hoja.getRow(indice);
                if (fila == null || filaVacia(fila, formatter, evaluator)) {
                    continue;
                }
                total++;
                int numeroFila = indice + Numeros.UNO;
                validarPrecisionIdentificadores(fila, numeroFila, columnas, errores);
                DatosImportacionProducto datos = construirDatos(fila, formatter, evaluator, columnas);
                validarEstructuraFila(datos, numeroFila, numeros, codigos, skus, errores);
            }

            if (!errores.isEmpty()) {
                throw new BusinessException(String.join(" | ", errores));
            }
            return total;
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    /** Procesa todas las filas, aislando cada transacción, y notifica el avance tras cada fila. */
    public ImportacionProductosResponseDTO procesar(
            byte[] contenido,
            Consumer<ImportacionProductosResponseDTO> progreso
    ) {
        ImportacionProductosResponseDTO resultado = nuevoResultado();
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
                procesarFila(fila, indice + Numeros.UNO, formatter, evaluator, columnas, resultado);
                resultado.setFilasProcesadas(resultado.getFilasProcesadas() + Numeros.UNO);
                progreso.accept(resultado);
            }

            adjuntarArchivoErrores(resultado);
            progreso.accept(resultado);
            return resultado;
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    private ImportacionProductosResponseDTO nuevoResultado() {
        return ImportacionProductosResponseDTO.builder()
                .filasProcesadas(Numeros.CERO)
                .productosCreados(Numeros.CERO)
                .marcasCreadas(Numeros.CERO)
                .categoriasCreadas(Numeros.CERO)
                .errores(new ArrayList<>())
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
            resultado.getErrores().add(ErrorImportacionProductoDTO.builder()
                    .fila(numeroFila)
                    .numeroProducto(datos.numero())
                    .marca(datos.marca())
                    .categoria(datos.categoria())
                    .codigoBarras(datos.codigoBarras())
                    .nombreProducto(datos.nombre())
                    .sku(datos.sku())
                    .mensaje(obtenerCausaReal(excepcion))
                    .build());
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

    /** Valida campos mínimos y duplicados internos del archivo antes de iniciar cualquier inserción. */
    private void validarEstructuraFila(
            DatosImportacionProducto datos,
            int numeroFila,
            java.util.Set<String> numeros,
            java.util.Set<String> codigos,
            java.util.Set<String> skus,
            List<String> errores
    ) {
        if (datos.numero() == null || datos.numero().isBlank()) {
            errores.add("Fila " + numeroFila + ": Número de Producto vacío.");
        } else if (!numeros.add(datos.numero().trim().toUpperCase(Locale.ROOT))) {
            errores.add("Fila " + numeroFila + ": Número de Producto duplicado dentro del Excel (" + datos.numero() + ").");
        }
        if (datos.nombre() == null || datos.nombre().isBlank()) {
            errores.add("Fila " + numeroFila + ": Nombre del Producto vacío.");
        }
        if (datos.codigoBarras() != null && !datos.codigoBarras().isBlank()) {
            String codigo = datos.codigoBarras().trim();
            if (!codigos.add(codigo)) {
                errores.add("Fila " + numeroFila + ": Código de Barras duplicado dentro del Excel (" + codigo + ").");
            }
        }
        if (datos.sku() != null && !datos.sku().isBlank()) {
            String sku = datos.sku().trim().toUpperCase(Locale.ROOT);
            if (!skus.add(sku)) {
                errores.add("Fila " + numeroFila + ": SKU duplicado dentro del Excel (" + datos.sku() + ").");
            }
        }
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

    /** Bloquea identificadores numéricos que Excel pudo redondear por superar quince dígitos. */
    private void validarPrecisionCelda(
            Row fila,
            int columna,
            int numeroFila,
            String nombreCampo,
            List<String> errores
    ) {
        Cell celda = fila.getCell(columna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cellValueReader.exceedsSafeExcelIntegerPrecision(celda)) {
            errores.add("Fila " + numeroFila + ": " + nombreCampo
                    + " supera 15 dígitos y está almacenado como número. Formatea esa columna como Texto para evitar pérdida de precisión.");
        }
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

    private String obtenerCausaReal(RuntimeException excepcion) {
        Throwable causa = excepcion;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getMessage() == null || causa.getMessage().isBlank()
                ? MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO
                : causa.getMessage();
    }

    /** Genera el Excel final con resumen y detalle exacto de filas rechazadas. */
    private void adjuntarArchivoErrores(ImportacionProductosResponseDTO resultado) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet resumen = workbook.createSheet("Resumen");
            resumen.createRow(0).createCell(0).setCellValue("Procesados");
            resumen.getRow(0).createCell(1).setCellValue(resultado.getFilasProcesadas());
            resumen.createRow(1).createCell(0).setCellValue("Exitosos");
            resumen.getRow(1).createCell(1).setCellValue(resultado.getProductosCreados());
            resumen.createRow(2).createCell(0).setCellValue("Errores");
            resumen.getRow(2).createCell(1).setCellValue(resultado.getErrores().size());
            resumen.createRow(3).createCell(0).setCellValue("Marcas creadas");
            resumen.getRow(3).createCell(1).setCellValue(resultado.getMarcasCreadas());
            resumen.createRow(4).createCell(0).setCellValue("Categorías creadas");
            resumen.getRow(4).createCell(1).setCellValue(resultado.getCategoriasCreadas());

            Sheet rechazados = workbook.createSheet(ConstantesImportacionProducto.HOJA_RECHAZADOS);
            String[] headers = {
                    ConstantesImportacionProducto.ENCABEZADO_FILA,
                    ConstantesImportacionProducto.ENCABEZADO_NUMERO,
                    ConstantesImportacionProducto.ENCABEZADO_MARCA,
                    ConstantesImportacionProducto.ENCABEZADO_CATEGORIA,
                    ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS,
                    ConstantesImportacionProducto.ENCABEZADO_NOMBRE,
                    ConstantesImportacionProducto.ENCABEZADO_SKU,
                    ConstantesImportacionProducto.ENCABEZADO_MOTIVO
            };
            Row header = rechazados.createRow(0);
            for (int indice = 0; indice < headers.length; indice++) {
                header.createCell(indice).setCellValue(headers[indice]);
            }
            for (int indice = 0; indice < resultado.getErrores().size(); indice++) {
                ErrorImportacionProductoDTO error = resultado.getErrores().get(indice);
                Row row = rechazados.createRow(indice + 1);
                row.createCell(0).setCellValue(error.getFila());
                row.createCell(1).setCellValue(seguro(error.getNumeroProducto()));
                row.createCell(2).setCellValue(seguro(error.getMarca()));
                row.createCell(3).setCellValue(seguro(error.getCategoria()));
                row.createCell(4).setCellValue(seguro(error.getCodigoBarras()));
                row.createCell(5).setCellValue(seguro(error.getNombreProducto()));
                row.createCell(6).setCellValue(seguro(error.getSku()));
                row.createCell(7).setCellValue(seguro(error.getMensaje()));
            }
            for (int indice = 0; indice < headers.length; indice++) {
                rechazados.autoSizeColumn(indice);
            }
            workbook.write(output);
            resultado.setArchivoErroresNombre(ConstantesImportacionProducto.ARCHIVO_ERRORES_NOMBRE);
            resultado.setArchivoErroresBase64(Base64.getEncoder().encodeToString(output.toByteArray()));
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    private String seguro(String valor) {
        return valor == null ? "" : valor;
    }
}
