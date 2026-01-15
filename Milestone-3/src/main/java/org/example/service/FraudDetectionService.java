package org.example.service;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FraudDetectionService {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private FraudMLService mlService;

    @Autowired
    private EmailService emailService;

    // Store consecutive fraud counts per Account ID to track "constant" attempts
    private final Map<String, Integer> fraudStreak = new ConcurrentHashMap<>();

    public Transaction processTransaction(Transaction transaction) {
        transaction.setTimestamp(LocalDateTime.now());
        boolean isFraud = false;
        StringBuilder reason = new StringBuilder();

        // --- EXISTING RULES ---

        // Rule 1: High Amount
        if (transaction.getAmount() > 50000) {
            isFraud = true;
            reason.append("Rule 1: Limit Exceeded. ");
        }

        // Rule 2: Blacklisted Merchant
        if ("CryptoStore".equalsIgnoreCase(transaction.getMerchant()) || "Casino".equalsIgnoreCase(transaction.getMerchant())) {
            isFraud = true;
            reason.append("Rule 19: High Risk Merchant. ");
        }

        // ML Check
        if (!isFraud) {
            double riskScore = mlService.predictRisk(transaction);
            int scorePercent = (int) (riskScore * 100);

            if (scorePercent > 70) {
                isFraud = true;
                reason.append("ML Alert: Anomalous Pattern (Confidence: " + scorePercent + "%).");
            }
        }

        transaction.setFraud(isFraud);
        transaction.setFraudReason(reason.toString());

        // --- NEW: CONSECUTIVE FRAUD LOGIC ---
        String accountId = transaction.getAccountId();

        if (isFraud) {
            // Increment fraud count for this account
            int count = fraudStreak.getOrDefault(accountId, 0) + 1;
            fraudStreak.put(accountId, count);

            System.out.println("Alert Level for " + accountId + ": " + count + "/5");

            // CHECK: If constantly fraud > 4 times (i.e., on the 5th time)
            if (count > 4) {
                System.out.println("CRITICAL: " + accountId + " exceeded fraud tolerance. Sending Email.");
                emailService.sendFraudAlert(transaction);

                // Reset counter to 0 so we don't spam emails for the 6th, 7th, 8th attempt
                fraudStreak.put(accountId, 0);
            }
        } else {
            // If the transaction is CLEAN, break the streak
            if (fraudStreak.containsKey(accountId)) {
                fraudStreak.remove(accountId);
                System.out.println("Streak broken for " + accountId + ". Counter reset.");
            }
        }

        return repository.save(transaction);
    }
}