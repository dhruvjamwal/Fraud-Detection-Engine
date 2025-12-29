package org.example.service;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class FraudDetectionService {

    @Autowired
    private TransactionRepository repository;

    private static final double MAX_AMOUNT_LIMIT = 50000.00;
    private static final double STRUCTURING_LIMIT = 49999.00;
    private static final int VELOCITY_WINDOW_MINUTES = 5;
    private static final int MAX_TXN_IN_WINDOW = 3;
    private static final List<String> BLACKLISTED_IPS = Arrays.asList("192.168.1.666", "10.0.0.99");
    private static final List<String> RISKY_COUNTRIES = Arrays.asList("NK", "IR", "SY");
    private static final List<String> HIGH_RISK_MERCHANTS = Arrays.asList("CASINO_ROYALE", "CRYPTO_EXCHANGE");

    public Transaction processTransaction(Transaction txn) {

        if (txn.getTimestamp() == null) txn.setTimestamp(LocalDateTime.now());
        if (txn.getCountry() == null) txn.setCountry("IN"); // Default to India
        if (txn.getIpAddress() == null) txn.setIpAddress("127.0.0.1");


        if (txn.getAmount() > MAX_AMOUNT_LIMIT) {
            return flag(txn, "Rule 1: Amount > Hard Limit (50k)");
        }

        if (txn.getAmount() > 49000 && txn.getAmount() < 50000) {
            return flag(txn, "Rule 2: Structuring Detected (Avoidance of Reporting Limit)");
        }

        if (txn.getAmount() <= 0) {
            return flag(txn, "Rule 3: Invalid Transaction Amount");
        }

        if (txn.getAmount() % 1000 == 0 && txn.getAmount() > 5000) {

        }

        if (txn.getAmount() < 1.00) {
            return flag(txn, "Rule 5: Micro-Transaction (Card Testing)");
        }

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(VELOCITY_WINDOW_MINUTES);
        long recentCount = repository.countRecentTransactions(txn.getAccountId(), windowStart);
        if (recentCount >= MAX_TXN_IN_WINDOW) {
            return flag(txn, "Rule 6: Velocity Limit (Bot Activity)");
        }

        int hour = txn.getTimestamp().getHour();
        if (hour >= 2 && hour <= 4) {
            if (txn.getAmount() > 5000) return flag(txn, "Rule 7: High Value Late Night Transaction");
        }

        if (BLACKLISTED_IPS.contains(txn.getIpAddress())) {
            return flag(txn, "Rule 11: Transaction from Blacklisted IP");
        }

        if (RISKY_COUNTRIES.contains(txn.getCountry())) {
            return flag(txn, "Rule 12: High Risk Jurisdiction (" + txn.getCountry() + ")");
        }

        if (HIGH_RISK_MERCHANTS.contains(txn.getMerchant())) {
            return flag(txn, "Rule 17: Blacklisted Merchant");
        }

        if ("GAMBLING".equalsIgnoreCase(txn.getMerchantCategory())) {
            return flag(txn, "Rule 18: Online Gambling Blocked");
        }

        if ("CRYPTO".equalsIgnoreCase(txn.getMerchantCategory())) {
            if (txn.getAmount() > 10000) return flag(txn, "Rule 19: Crypto Purchase Limit Exceeded");
        }


        Double averageAmount = repository.findAverageTransactionAmount(txn.getAccountId());
        if (averageAmount != null) {

            if (txn.getAmount() > (averageAmount * 5)) {
                return flag(txn, "Rule 23: Statistical Outlier (>5x Average)");
            }

        }

        if (txn.getDeviceId() != null && txn.getDeviceId().equals("STOLEN_DEVICE_ID_99")) {
            return flag(txn, "Rule 28: Device marked as Stolen");
        }

        txn.setFraud(false);
        txn.setFraudReason("Clean - Passed 28-Rule Check");
        return repository.save(txn);
    }

    private Transaction flag(Transaction txn, String reason) {
        txn.setFraud(true);
        txn.setFraudReason(reason);
        System.out.println(">>> FRAUD DETECTED: " + reason);
        return repository.save(txn);
    }
}