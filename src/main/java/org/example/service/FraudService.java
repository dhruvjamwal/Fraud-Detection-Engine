package org.example.service;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudService {

    @Autowired
    private TransactionRepository repository;

    // A hardcoded "Blacklist" for demo purposes
    private static final List<String> BLACKLISTED_MERCHANTS = List.of("DarkWeb", "UnknownSite", "HackerStore");

    public Transaction processTransaction(Transaction txn) {
        txn.setTimestamp(LocalDateTime.now());
        txn.setFraud(false); // Default to safe
        txn.setFraudReason("Safe");

        // --- RULE 1: High Amount Check ---
        if (txn.getAmount() > 50000) {
            txn.setFraud(true);
            txn.setFraudReason("High Value Transaction Alert");
        }

        // --- RULE 2: Blacklisted Merchant Check ---
        // If the merchant name is in our blacklist, flag it.
        else if (BLACKLISTED_MERCHANTS.contains(txn.getMerchant())) {
            txn.setFraud(true);
            txn.setFraudReason("Suspicious Merchant (" + txn.getMerchant() + ")");
        }

        // --- RULE 3: Velocity Check (High Frequency) ---
        // Check how many transactions this user made in the last 5 minutes
        else {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            int recentCount = repository.countByAccountIdAndTimestampAfter(txn.getAccountId(), fiveMinutesAgo);

            if (recentCount >= 3) {
                txn.setFraud(true);
                txn.setFraudReason("High Frequency");
            }
        }

        // Save result to Database
        return repository.save(txn);
    }

    public List<Transaction> getFraudTransactions() {
        return repository.findByIsFraudTrue();
    }
}