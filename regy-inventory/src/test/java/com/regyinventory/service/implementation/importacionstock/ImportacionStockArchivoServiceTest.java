package com.regyinventory.service.implementation.importacionstock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.regyinventory.entities.Producto;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.service.implementation.OperacionSupport;
import com.regyinventory.utils.excel.ExcelCellValueReader;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** Validación cruzada de identificadores y rechazo individual sin registrar movimientos. */
class ImportacionStockArchivoServiceTest {
    private final IProductoRepository productos = mock(IProductoRepository.class);
    private final IUbicacionRepository ubicaciones = mock(IUbicacionRepository.class);
    private final OperacionSupport permisos = mock(OperacionSupport.class);
    private final ImportacionStockArchivoService servicio =
            new ImportacionStockArchivoService(new ExcelCellValueReader(), productos, ubicaciones, permisos);

    @Test
    void identificaPorNumeroYRechazaSoloLasFilasConSkuInconsistente() throws Exception {
        Producto producto = Producto.builder().numero("825").nombre("Tratamiento")
                .sku("825-CP1-CAB").codigoBarras("8809450011251").build();
        producto.setId(1L);
        Ubicacion caja = Ubicacion.builder().codigo("CAJA-A01-01")
                .nombre("Caja A01").tipo(TipoUbicacion.CAJA).build();
        caja.setId(10L);
        when(productos.findByNumeroIgnoreCase("825")).thenReturn(Optional.of(producto));
        when(ubicaciones.findByCodigoIgnoreCase("CAJA-A01-01")).thenReturn(Optional.of(caja));

        byte[] excel = archivo(new String[][]{
                {"825", "825-CP1-CAB", "8809450011251", "CAJA-A01-01", "12", "2027-06-30", "Ingreso"},
                {"825", "SKU-INCORRECTO", "8809450011251", "CAJA-A01-01", "8", "2027-06-30", "Ingreso"},
                {"825", "825-CP1-CAB", "8809450011251", "CAJA-A01-01", "5", "2027-06-30", "Ingreso"}
        });
        ValidacionImportacionStock resultado = servicio.validar(excel);
        assertEquals(3, resultado.total());
        assertEquals(2, resultado.validas().size());
        assertEquals(1, resultado.errores().size());
        assertEquals(3, resultado.errores().get(0).getFila());
        assertTrue(resultado.errores().get(0).getMensaje().contains("SKU no corresponde"));
    }

    private byte[] archivo(String[][] datos) throws Exception {
        String[] headers = {"Número de Producto", "SKU", "Código de Barras",
                "Código de Ubicación", "Cantidad", "Fecha de Vencimiento", "Observaciones"};
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet("Ingreso stock");
            Row cabecera = hoja.createRow(0);
            for (int c = 0; c < headers.length; c++) cabecera.createCell(c).setCellValue(headers[c]);
            for (int f = 0; f < datos.length; f++) {
                Row fila = hoja.createRow(f + 1);
                for (int c = 0; c < headers.length; c++) fila.createCell(c).setCellValue(datos[f][c]);
            }
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
