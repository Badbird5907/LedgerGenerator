package dev.badbird.ledger.util;

import org.apache.poi.ss.usermodel.Cell;

public class CellUtil {
    public static void copyCell(Cell from, Cell target) {
        if (from == null || target == null)
            return;
        target.setCellType(from.getCellType());
        switch (from.getCellType()) {
            case STRING:
                target.setCellValue(from.getStringCellValue());
                break;
            case NUMERIC:
                target.setCellValue(from.getNumericCellValue());
                break;
            case BOOLEAN:
                target.setCellValue(from.getBooleanCellValue());
                break;
            case FORMULA:
                target.setCellFormula(from.getCellFormula());
                break;
            default:
                break;
        }
        target.setCellStyle(from.getCellStyle());
    }
}
