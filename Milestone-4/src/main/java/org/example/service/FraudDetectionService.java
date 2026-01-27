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

    private final Map<String, Integer> velocityCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastTxnTime = new ConcurrentHashMap<>();

    public Transaction processTransaction(Transaction transaction) {
        if (transaction.getTimestamp() == null) transaction.setTimestamp(LocalDateTime.now());
        if (transaction.getLocation() == null) transaction.setLocation(transaction.getCountry());

        boolean isFraud = false;
        StringBuilder reason = new StringBuilder();

        double rawRisk = mlService.predictRisk(transaction);
        double scorePercent = rawRisk * 100;
        transaction.setMlScore(scorePercent);

        // --- RULE 1: Amount Analysis ---
        if (transaction.getAmount() > 50000) {
            isFraud = true;
            reason.append("Rule 1: Amount Analysis (Limit Exceeded). ");
        }

        // --- RULE 2: Location Risk ---
        String country = transaction.getCountry() != null ? transaction.getCountry().toUpperCase() : "UNKNOWN";
        if ("NK".equals(country) || "IR".equals(country) || "SY".equals(country)) {
            isFraud = true;
            reason.append("Rule 2: Location Risk (Sanctioned Region). ");
        }

        // --- RULE 3: Timing Patterns ---
        int hour = transaction.getTimestamp().getHour();
        if (hour >= 2 && hour <= 4 && transaction.getAmount() > 10000) {
            isFraud = true;
            reason.append("Rule 3: Timing Patterns (High Value at 3 AM). ");
        }

        // --- RULE 4: Velocity Check ---
        String accId = transaction.getAccountId();
        int recentCount = velocityCache.getOrDefault(accId, 0);
        LocalDateTime lastTime = lastTxnTime.getOrDefault(accId, LocalDateTime.now().minusHours(1));

        if (transaction.getTimestamp().isBefore(lastTime.plusSeconds(10))) {
            recentCount++;
        } else {
            recentCount = 1;
        }
        velocityCache.put(accId, recentCount);
        lastTxnTime.put(accId, transaction.getTimestamp());

        if (recentCount > 3) {
            isFraud = true;
            reason.append("Rule 4: Velocity Check (Rapid Transactions). ");
        }

        // --- RULE 5: Behavioral Analysis ---
        Double avgAmount = repository.findAverageTransactionAmount(accId);
        if (avgAmount != null && avgAmount > 0 && transaction.getAmount() > (avgAmount * 10)) {
            isFraud = true;
            reason.append("Rule 5: Behavioral Analysis (10x Normal Spend). ");
        }

        // --- RULE 6: Device Risk ---
        String device = transaction.getDeviceId();
        if (device != null && (device.startsWith("ROOT") || device.contains("EMULATOR"))) {
            isFraud = true;
            reason.append("Rule 6: Device Risk (Rooted/Jailbroken). ");
        }

        // --- RULE 7: IP Analysis ---
        String ip = transaction.getIpAddress();
        if (ip != null && (ip.startsWith("104.22") || ip.startsWith("162.158"))) {
            isFraud = true;
            reason.append("Rule 7: IP Analysis (Known Proxy/VPN). ");
        }

        // --- RULE 8: Geolocation Mismatch ---
        // Simulating mismatch if IP indicates US but Country is IN
        if (ip != null && ip.startsWith("192.") && "US".equals(country)) {
            // Local IP often implies home country, logic simplified for demo
        }
        if ("US".equals(country) && transaction.getMerchant().contains("RuPay")) {
            isFraud = true;
            reason.append("Rule 8: Geolocation Mismatch. ");
        }

        // --- RULE 9: Transaction Type Risk ---
        String cat = transaction.getMerchantCategory();
        if ("CRYPTO".equalsIgnoreCase(cat) || "GAMBLING".equalsIgnoreCase(cat)) {
            isFraud = true;
            reason.append("Rule 9: Transaction Type Risk (Blacklisted Category). ");
        }

        // --- RULE 10: Historical Profile ---
        long pastFrauds = repository.countByIsFraud(true);
        if (pastFrauds > 5 && transaction.getAmount() > 10000) {
            // Note: In real logic, this would be specific to the user (countByAccountIdAndIsFraud)
            // For this demo, we use global system state to trigger the rule
            isFraud = true;
            reason.append("Rule 10: Historical Profile (High Risk Account). ");
        }

        // --- ML FALLBACK ---
        if (!isFraud && scorePercent > 80) {
            isFraud = true;
            reason.append("ML Alert: Anomalous Pattern detected by RandomForest.");
        }

        transaction.setFraud(isFraud);
        transaction.setFraudReason(isFraud ? reason.toString().trim() : "Clean");

        if (isFraud) {
            if (recentCount >= 5) {
                emailService.sendFraudAlert(transaction);
                velocityCache.put(accId, 0);
            }
        }

        return repository.save(transaction);
    }
}