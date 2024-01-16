package dev.badbird.ledger.objects.journal;

import dev.badbird.ledger.objects.ledger.LedgerAccount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class JournalAccount {
    private String name;
    private List<JournalTransaction> transactions;

    public LedgerAccount toLedgerAccount() {
        return new LedgerAccount(name, new ArrayList<>());
    }
}
