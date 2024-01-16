package dev.badbird.ledger.objects.journal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Month;
import java.util.List;

@Data
@AllArgsConstructor
public class JournalEntry {
    private int year;
    private Month month;
    private int date;
    private List<String> particulars;
    private List<JournalTransaction> transactions;

    public JournalEntry updateTransactions() {
        for (JournalTransaction transaction : transactions) {
            transaction.setEntry(this);
        }
        return this;
    }
}
