package com.regyinventory.service.implementation.importacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.utils.constants.importacion.ConstantesImportacionProducto;
import com.regyinventory.utils.excel.ExcelCellValueReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** Regresión: duplicados, rechazo parcial y consolidación del Excel. */
class ProductoImportacionArchivoServiceTest {

    private final ProductoImportacionFilaService filas = mock(ProductoImportacionFilaService.class);
    private final ProductoImportacionArchivoService servicio =
            new ProductoImportacionArchivoService(filas, new ExcelCellValueReader());

    @Test
    void rechazaTodasLasAparicionesDuplicadasPeroImportaLasFilasValidas() throws Exception {
        byte[] archivo = excel(new String[][]{
                {"68", "PODL", "MAS", "8800292450116", "Oat Ceramide HydroMask", "68-PODL-MAS"},
                {"68", "PODL", "MAS", "8800292450116", "Oat Ceramide HydroMask", "68-PODL-MAS"},
                {"69", "Otra", "MAS", "8800292450093", "Rice Glutathione", "69-OTRA-MAS"}
        });
        ValidacionImportacionProducto validacion = servicio.validar(archivo);
        assertEquals(3, validacion.total());
        assertEquals(2, validacion.filasInvalidas().size());
        assertTrue(validacion.filasInvalidas().contains(2));
        assertTrue(validacion.filasInvalidas().contains(3));
        assertEquals(2, validacion.errores().size());
        assertTrue(validacion.errores().get(0).getMensaje().contains("Filas: 2, 3"));
        assertTrue(validacion.errores().get(0).getMensaje().contains("SKU duplicado"));

        when(filas.importarFila(any())).thenReturn(new ResultadoImportacionFila(true, true));
        ImportacionProductosResponseDTO resultado = servicio.procesar(archivo, validacion, parcial -> {});
        assertEquals(3, resultado.getFilasProcesadas());
        assertEquals(1, resultado.getProductosCreados());
        assertEquals(2, resultado.getErrores().size());
        assertEquals(1, resultado.getMarcasCreadas());
        assertEquals(1, resultado.getCategoriasCreadas());
        verify(filas).importarFila(any());

        try (Workbook reporte = new XSSFWorkbook(new ByteArrayInputStream(
                Base64.getDecoder().decode(resultado.getArchivoErroresBase64())))) {
            assertEquals(2, reporte.getNumberOfSheets());
            assertEquals(1, reporte.getSheet("Rechazados").getLastRowNum());
            assertEquals("2, 3", reporte.getSheet("Rechazados").getRow(1).getCell(0).getStringCellValue());
            assertEquals(1, (int) reporte.getSheet("Resumen").getRow(1).getCell(1).getNumericCellValue());
        }
    }

    private byte[] excel(String[][] datos) throws Exception {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet("Productos");
            Row encabezado = hoja.createRow(0);
            for (int c = 0; c < ConstantesImportacionProducto.ENCABEZADOS.length; c++) {
                encabezado.createCell(c).setCellValue(ConstantesImportacionProducto.ENCABEZADOS[c]);
            }
            for (int f = 0; f < datos.length; f++) {
                Row fila = hoja.createRow(f + 1);
                for (int c = 0; c < datos[f].length; c++) fila.createCell(c).setCellValue(datos[f][c]);
            }
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
