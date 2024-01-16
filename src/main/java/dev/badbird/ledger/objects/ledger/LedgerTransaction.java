package dev.badbird.ledger.objects.ledger;

import dev.badbird.ledger.objects.journal.JournalAccount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Month;

@Data
@AllArgsConstructor
public class LedgerTransaction {
    private String particulars;
    private String pr;
    private double dr;
    private double cr;
    private LedgerAccount account;
    private Ledger ledger;
    private Month month;
    private int year;
    private int date;

    public double applyToBal(double in) {
        if (dr != 0)
            return in + dr;
        else
            return in - cr;
    }
}
