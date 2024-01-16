package dev.badbird.ledger.objects.ledger;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LedgerAccount {
    private String name;
    private List<LedgerTransaction> transactions;
}
