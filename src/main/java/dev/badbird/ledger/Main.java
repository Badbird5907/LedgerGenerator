package dev.badbird.ledger;

import dev.badbird.ledger.objects.journal.Journal;
import dev.badbird.ledger.objects.journal.JournalAccount;
import dev.badbird.ledger.objects.journal.JournalEntry;
import dev.badbird.ledger.objects.ledger.Ledger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = new File("in.xlsx");
        if (!file.exists()) {
            System.out.println("File not found!");
            return;
        }
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        String sheetName = System.getProperty("sheet", "Journal");
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            System.out.println("Sheet " + sheetName + " not found!");
            return;
        }
        Journal journal = new Journal().parse(sheet);
        System.out.println("Done parsing journal!");
        Map<JournalAccount, Ledger> ledger = journal.generateLedgers();
        System.out.println("Accounts: " + ledger.size());
        journal.getAccounts().forEach(
                (name, account) -> {
                    System.out.println("Account: " + name);
                }
        );
        System.out.println("Done generating ledgers!");
        Ledger.generateLedgerSheet(ledger.values(), workbook);
        System.out.println("Done generating ledger sheet!");

        FileOutputStream fos = new FileOutputStream("out-" + System.currentTimeMillis() + ".xlsx");
        workbook.write(fos);
        workbook.close();
    }

}