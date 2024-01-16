package dev.badbird.ledger.objects.journal;

import dev.badbird.ledger.objects.ledger.Ledger;
import dev.badbird.ledger.objects.ledger.LedgerTransaction;
import dev.badbird.ledger.util.DateUtil;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.Month;
import java.util.*;

@Data
public class Journal {
    private List<JournalEntry> entries = new ArrayList<>();
    private Map<String, JournalAccount> accounts = new HashMap<>();
    public Journal parse(Sheet sheet) {
        // Month - A | Date - B | Particulars - C | PR - D | DR - E | CR - F
        Iterator<Row> rowIterator = sheet.rowIterator();
        int skipRows = 3; // skip the first 3 rows as they are headers
        int year = Integer.getInteger("year", 2023);
        List<Row> rowBuffer = new ArrayList<>();
        JournalEntry entry;
        int lastDate = 0;
        Month lastMonth = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (skipRows > 0) {
                skipRows--;
                continue;
            }
            Cell rowA = row.getCell(0);
            Cell rowB = row.getCell(1);
            String rowParticulars = row.getCell(2).getStringCellValue();
            // if a and b is filled out and buffer isn't empty, throw err
            if ((rowA != null && rowB != null) && !rowBuffer.isEmpty() && (rowA.getCellType() != CellType.BLANK && rowB.getCellType() != CellType.BLANK))
                throw new IllegalStateException("Row buffer is not empty, but row A and B are not blank! (Row " + row.getRowNum() + ")");
            if (rowParticulars.isBlank()) { // signifies the end of the journal entry
                if (rowBuffer.isEmpty()) // if the row buffer is empty, we're done
                    break;
                entry = parseEntry(rowBuffer, year, lastDate, lastMonth);
                lastDate = entry.getDate();
                lastMonth = entry.getMonth();
                entries.add(entry);
                rowBuffer.clear();
                continue;
            }
            rowBuffer.add(row);
        }
        return this;
    }

    private JournalEntry parseEntry(List<Row> rowBuffer, int year, int lastDate, Month lastMonth) {
        Row firstRow = rowBuffer.get(0);
        System.out.println("Parsing entry: " + firstRow.getRowNum());
        Cell rowA = firstRow.getCell(0);
        String monthStr = rowA == null ? lastMonth.name() : rowA.getStringCellValue().trim();
        Cell rowB = firstRow.getCell(1);
        String rowParticulars = firstRow.getCell(2).getStringCellValue().trim();
        Month month = DateUtil.parseMonth(monthStr);
        System.out.println("Month: " + month + " | " + monthStr);
        int date = rowB == null ? lastDate : (int) rowB.getNumericCellValue();
        List<JournalTransaction> transactions = new ArrayList<>();
        List<String> entryDescription = new ArrayList<>();
        for (Row row : rowBuffer) {
            String particulars = row.getCell(2).getStringCellValue().trim();
            Cell pr = row.getCell(3);
            Cell drCell = row.getCell(4);
            Cell crCell = row.getCell(5);
            if ((drCell == null || drCell.getCellType() == CellType.BLANK) && (crCell == null || crCell.getCellType() == CellType.BLANK)) {
                entryDescription.add(particulars);
                continue;
            }
            JournalAccount account = getOrCreateAccount(particulars);
            double dr = drCell == null ? 0 : drCell.getNumericCellValue();
            double cr = crCell == null ? 0 : crCell.getNumericCellValue();
            System.out.println("DR: " + dr + " | CR: " + cr);
            JournalTransaction transaction = new JournalTransaction(particulars, "", dr, cr, null);
            transactions.add(transaction);
            account.getTransactions().add(transaction);
        }
        return new JournalEntry(year, month, date, entryDescription, transactions).updateTransactions();
    }

    private JournalAccount getOrCreateAccount(String rowParticulars) {
        if (accounts.containsKey(rowParticulars.toLowerCase()))
            return accounts.get(rowParticulars.toLowerCase());
        JournalAccount account = new JournalAccount(rowParticulars, new ArrayList<>());
        accounts.put(rowParticulars.toLowerCase(), account);
        return account;
    }

    public Map<JournalAccount, Ledger> generateLedgers() {
        Map<JournalAccount, Ledger> ledgers = new HashMap<>();
        accounts.forEach((name, account) -> {
            Ledger ledger = new Ledger(account.toLedgerAccount(), new ArrayList<>());
            ledgers.put(account, ledger);
            account.getTransactions().forEach(transaction -> {
                LedgerTransaction ledgerTransaction = new LedgerTransaction(transaction.getParticulars(), // particulars should be blank
                        "J1", transaction.getDr(), transaction.getCr(), ledger.getAccount(), ledger,
                        transaction.getEntry().getMonth(), transaction.getEntry().getYear(), transaction.getEntry().getDate()
                        );
                ledger.getTransactions().add(ledgerTransaction);
            });
        });
        return ledgers;
    }
}
