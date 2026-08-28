package com.regyinventory.utils.constants.importacion;

import com.regyinventory.utils.constants.numeros.Numeros;

public final class ConstantesImportacionProducto {

    public static final int COLUMNA_NUMERO = Numeros.CERO;
    public static final int COLUMNA_MARCA = Numeros.UNO;
    public static final int COLUMNA_CATEGORIA = Numeros.DOS;
    public static final int COLUMNA_CODIGO_BARRAS = Numeros.TRES;
    public static final int COLUMNA_NOMBRE = Numeros.CUATRO;
    public static final int COLUMNA_SKU = Numeros.CINCO;
    public static final int TOTAL_COLUMNAS = Numeros.SEIS;

    public static final String ENCABEZADO_NUMERO = "Número de Producto";
    public static final String ENCABEZADO_MARCA = "Marca";
    public static final String ENCABEZADO_CATEGORIA = "Categoría";
    public static final String ENCABEZADO_CODIGO_BARRAS = "Código de Barras Completo";
    public static final String ENCABEZADO_NOMBRE = "Nombre del Producto";
    public static final String ENCABEZADO_SKU = "SKU";

    public static final String ENCABEZADO_FILA = "Fila";
    public static final String ENCABEZADO_MOTIVO = "Motivo del rechazo";
    public static final String ARCHIVO_ERRORES_NOMBRE = "productos_rechazados.xlsx";
    public static final String HOJA_RECHAZADOS = "Rechazados";

    public static final String[] ENCABEZADOS = {
            ENCABEZADO_NUMERO,
            ENCABEZADO_MARCA,
            ENCABEZADO_CATEGORIA,
            ENCABEZADO_CODIGO_BARRAS,
            ENCABEZADO_NOMBRE,
            ENCABEZADO_SKU
    };

    private ConstantesImportacionProducto() {
    }
}
