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

    private final Map<String, Integer> fraudStreak = new ConcurrentHashMap<>();

    public Transaction processTransaction(Transaction transaction) {
        transaction.setTimestamp(LocalDateTime.now());
        boolean isFraud = false;
        StringBuilder reason = new StringBuilder();
//Rules--------
        if (transaction.getAmount() > 50000) {
            isFraud = true;
            reason.append("Rule 1: Limit Exceeded. ");
        }

        if ("CryptoStore".equalsIgnoreCase(transaction.getMerchant()) || "Casino".equalsIgnoreCase(transaction.getMerchant())) {
            isFraud = true;
            reason.append("Rule 19: High Risk Merchant. ");
        }
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

        String accountId = transaction.getAccountId();

        if (isFraud) {
            int count = fraudStreak.getOrDefault(accountId, 0) + 1;
            fraudStreak.put(accountId, count);

            System.out.println("Alert Level for " + accountId + ": " + count + "/5");

            if (count > 4) {
                System.out.println("CRITICAL: " + accountId + " exceeded fraud tolerance. Sending Email.");
                emailService.sendFraudAlert(transaction);

                fraudStreak.put(accountId, 0);
            }
        } else {
            if (fraudStreak.containsKey(accountId)) {
                fraudStreak.remove(accountId);
                System.out.println("Streak broken for " + accountId + ". Counter reset.");
            }
        }

        return repository.save(transaction);
    }
}
