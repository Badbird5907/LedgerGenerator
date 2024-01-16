package dev.badbird.ledger.objects.journal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JournalTransaction {
    private String particulars;
    private String pr;
    private double dr;
    private double cr;

    private transient JournalEntry entry;
}
