package dev.badbird.ledger;

import dev.badbird.diffchecker.DiffChecker;
import dev.badbird.diffchecker.DiffCheckerImpl;
import dev.badbird.diffchecker.engine.DiffEngine;
import dev.badbird.diffchecker.engine.impl.stringsimilarity.impl.DiceCoefficientStrategyEngine;
import dev.badbird.diffchecker.engine.impl.stringsimilarity.impl.JaroStrategyEngine;
import dev.badbird.diffchecker.engine.impl.stringsimilarity.impl.JaroWinklerStrategyEngine;
import dev.badbird.diffchecker.engine.impl.stringsimilarity.impl.LevDistanceStrategyEngine;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        System.out.println("----------- Finding possible duplicates -----------");
        DiffChecker checker = new DiffCheckerImpl().init();
        Set<String> names = journal.getAccounts().keySet();
        List<String> seen = new ArrayList<>();
        for (String name : names) {
            if (seen.contains(name))
                continue;
            for (String name2 : names) {
                if (name.equals(name2))
                    continue;
                Class<? extends DiffEngine>[] engines = new Class[]{JaroWinklerStrategyEngine.class};
                for (Class<? extends DiffEngine> engine : engines) {
                    double similarity = checker.getSimilarity(name, name2, engine);
                    if (similarity > 0.80) {
                        System.out.println("[SIM] Similarity between " + name + " and " + name2 + " is " + (String.format("%.2f", similarity)) + " using engine " + engine.getSimpleName());
                    }
                }
            }
            seen.add(name);
        }
        System.out.println("Done generating ledgers!");
        Ledger.generateLedgerSheet(ledger.values(), workbook);
        System.out.println("Done generating ledger sheet!");

        FileOutputStream fos = new FileOutputStream("out-" + System.currentTimeMillis() + ".xlsx");
        workbook.write(fos);
        workbook.close();
    }

}