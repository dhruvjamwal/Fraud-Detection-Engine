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

    private static final List<String> BLACKLISTED_MERCHANTS = List.of("DarkWeb", "UnknownSite", "HackerStore");

    public Transaction processTransaction(Transaction txn) {
        txn.setTimestamp(LocalDateTime.now());
        txn.setFraud(false); // Default to safe
        txn.setFraudReason("Safe");

        if (txn.getAmount() > 50000) {
            txn.setFraud(true);
            txn.setFraudReason("High Value Transaction Alert");
        }


        else if (BLACKLISTED_MERCHANTS.contains(txn.getMerchant())) {
            txn.setFraud(true);
            txn.setFraudReason("Suspicious Merchant (" + txn.getMerchant() + ")");
        }


        else {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            int recentCount = repository.countByAccountIdAndTimestampAfter(txn.getAccountId(), fiveMinutesAgo);

            if (recentCount >= 3) {
                txn.setFraud(true);
                txn.setFraudReason("High Frequency");
            }
        }

        return repository.save(txn);
    }

    public List<Transaction> getFraudTransactions() {
        return repository.findByIsFraudTrue();
    }
}
