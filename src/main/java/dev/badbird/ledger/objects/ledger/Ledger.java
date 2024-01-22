package dev.badbird.ledger.objects.ledger;

import dev.badbird.ledger.util.CellUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.Month;
import java.util.*;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
public class Ledger {
    private LedgerAccount account;
    private List<LedgerTransaction> transactions;

    public static void generateLedgerSheet(Collection<Ledger> ledgerCol, Workbook workbook) {
        List<Ledger> sortedLegders = new ArrayList<>(ledgerCol);
        sortedLegders.sort(Comparator.comparing(o -> o.getAccount().getName()));

        Sheet sheet = workbook.createSheet("Ledger-New-" + System.currentTimeMillis());
        Sheet template = workbook.getSheet("Ledger-Template");
        // copy the template sheet to the new sheet
        int cols = 8;
        for (int i = 0; i < cols; i++) {
            sheet.setColumnWidth(i, template.getColumnWidth(i));
        }
        CellStyle style = template.getRow(1).getCell(0).getCellStyle();
        CellStyle bold = template.getRow(9).getCell(0).getCellStyle();
        Consumer<CellStyle> updateStyles = cellStyle -> {
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.GENERAL);
        };
        updateStyles.accept(style);
        updateStyles.accept(bold);

        List<Cell[]> templateHeader = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Row row = template.getRow(i);
            if (row == null)
                continue;
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell[] cells = new Cell[cols];
            int index = 0;
            while (cellIterator.hasNext()) {
                cells[index++] = cellIterator.next();
            }
            templateHeader.add(cells);
        }

        int offset = 0;
        int year = Integer.getInteger("year", 2023);


        double totalBalance = 0;
        double totalDr = 0;
        double totalCr = 0;
        for (Ledger ledger : sortedLegders) {
            applyTemplate(templateHeader, sheet, ++offset, ledger.getAccount(), template.getMergedRegions());
            offset += templateHeader.size();
            Row yearRow = sheet.createRow(offset++);
            Cell yearCell = yearRow.createCell(0);
            yearCell.setCellValue(year);
            yearCell.setCellStyle(style);
            for (int i = 0; i < cols - 1; i++) {
                Cell cell = yearRow.createCell(i + 1);
                cell.setCellStyle(style);
            }
            double balance = 0;
            double ledgerTotalDr = 0;
            double ledgerTotalCr = 0;
            Month lastMonth = null;
            for (LedgerTransaction transaction : ledger.getTransactions()) {
                balance = transaction.applyToBal(balance);
                totalBalance = transaction.applyToBal(totalBalance);
                totalDr += transaction.getDr();
                totalCr += transaction.getCr();
                ledgerTotalDr += transaction.getDr();
                ledgerTotalCr += transaction.getCr();
                Row row = sheet.createRow(offset++);
                Cell monthCell = row.createCell(0);
                monthCell.setCellStyle(style);
                Cell dateCell = row.createCell(1);
                dateCell.setCellStyle(style);
                if (lastMonth != transaction.getMonth()) {
                    monthCell.setCellValue(transaction.getMonth().name());
                    lastMonth = transaction.getMonth();
                }
                dateCell.setCellValue(transaction.getDate());
                Cell particularsCell = row.createCell(2);
                particularsCell.setCellValue(transaction.getParticulars().trim());
                particularsCell.setCellStyle(style);
                Cell prCell = row.createCell(3);
                prCell.setCellValue(transaction.getPr());
                prCell.setCellStyle(style);
                Cell drCell = row.createCell(4);
                drCell.setCellStyle(style);
                if (transaction.getDr() != 0) {
                    drCell.setCellValue(transaction.getDr());
                }
                Cell crCell = row.createCell(5);
                if (transaction.getCr() != 0) {
                    crCell.setCellValue(transaction.getCr());
                }
                crCell.setCellStyle(style);
                Cell drCrCell = row.createCell(6);
                drCrCell.setCellValue(balance >= 0 ? "Dr" : "Cr");
                drCrCell.setCellStyle(style);
                Cell balanceCell = row.createCell(7);
                balanceCell.setCellValue(Math.abs(balance));
                balanceCell.setCellStyle(style);
            }
            Row totalRow = sheet.createRow(offset++);
            Cell totalCell = totalRow.createCell(0);
            printTotal(cols, style, balance, ledgerTotalDr, ledgerTotalCr, totalRow, totalCell, bold);
        }

        Row totalRow = sheet.createRow(++offset);
        Cell totalCell = totalRow.createCell(0);
        printTotal(cols, style, totalBalance, totalDr, totalCr, totalRow, totalCell, bold);

    }

    private static void printTotal(int cols, CellStyle style, double balance, double ledgerTotalDr, double ledgerTotalCr, Row totalRow, Cell totalCell, CellStyle bold) {
        totalCell.setCellValue("Total");
        totalCell.setCellStyle(bold);
        for (int i = 0; i < cols - 1; i++) {
            Cell cell = totalRow.createCell(i + 1);
            cell.setCellStyle(style);
        }
        Cell totalDrCell = totalRow.createCell(4);
        totalDrCell.setCellValue(ledgerTotalDr);
        totalDrCell.setCellStyle(style);
        Cell totalCrCell = totalRow.createCell(5);
        totalCrCell.setCellValue(ledgerTotalCr);
        totalCrCell.setCellStyle(style);
        Cell totalDrCrCell = totalRow.createCell(6);
        totalDrCrCell.setCellValue(ledgerTotalDr >= ledgerTotalCr ? "Dr" : "Cr");
        totalDrCrCell.setCellStyle(style);
        Cell totalBalanceCell = totalRow.createCell(7);
        totalBalanceCell.setCellValue(Math.abs(balance));
        totalBalanceCell.setCellStyle(style);
    }

    private static Sheet applyTemplate(List<Cell[]> template, Sheet sheet, int offset, LedgerAccount account, List<CellRangeAddress> mergedRegions) {
        for (int i = 0; i < template.size(); i++) {
            Cell[] cells = template.get(i);
            Row row = sheet.createRow(i + offset);
            for (int j = 0; j < cells.length; j++) {
                Cell cell = cells[j];
                Cell newCell = row.createCell(j);
                CellUtil.copyCell(cell, newCell);
                if (newCell.getCellType() == CellType.STRING) {
                    String value = newCell.getStringCellValue();
                    if (value.contains("{account}")) {
                        value = value.replace("{account}", account.getName());
                        newCell.setCellValue(value);
                    }
                }
            }
        }
        // apply merged regions
        for (CellRangeAddress mergedRegion : mergedRegions) {
            CellRangeAddress newRegion = new CellRangeAddress(mergedRegion.getFirstRow() + offset, mergedRegion.getLastRow() + offset, mergedRegion.getFirstColumn(), mergedRegion.getLastColumn());
            sheet.addMergedRegion(newRegion);
        }
        return sheet;
    }
}
