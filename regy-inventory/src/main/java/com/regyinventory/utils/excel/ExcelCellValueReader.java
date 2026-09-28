package com.regyinventory.utils.excel;

import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.springframework.stereotype.Component;

/**
 * Lee valores de Excel sin convertir identificadores numéricos a notación científica.
 * Los códigos, números de producto y SKU se conservan como texto durante toda la importación.
 */
@Component
public class ExcelCellValueReader {

    /** Devuelve el valor de una celda como texto estable, evitando representaciones E+N. */
    public String read(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        if (cellType == CellType.NUMERIC) {
            String formatted = formatter.formatCellValue(cell, evaluator).trim();
            if (isPlainInteger(formatted)) {
                return formatted;
            }
            return normalizeNumericValue(cell.getNumericCellValue());
        }

        return normalizeScientificText(formatter.formatCellValue(cell, evaluator).trim());
    }

    /** Expande notación científica textual sin convertir el identificador a double. */
    private String normalizeScientificText(String value) {
        if (value != null && value.matches("[-+]?\\d+(?:\\.\\d+)?[eE][-+]?\\d+")) {
            return new BigDecimal(value).toPlainString();
        }
        return value;
    }

    /** Indica si una celda numérica supera la precisión segura de Excel para un identificador. */
    public boolean exceedsSafeExcelIntegerPrecision(Cell cell) {
        if (cell == null) {
            return false;
        }
        CellType type = cell.getCellType() == CellType.FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();
        if (type != CellType.NUMERIC) {
            return false;
        }
        String value = normalizeNumericValue(cell.getNumericCellValue()).replace("-", "");
        return value.matches("\\d+") && value.length() > 15;
    }

    private String normalizeNumericValue(double value) {
        String text = NumberToTextConverter.toText(value);
        if (text.contains("E") || text.contains("e")) {
            return new BigDecimal(text).toPlainString();
        }
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    private boolean isPlainInteger(String value) {
        return value.matches("[-+]?\\d+");
    }
}
