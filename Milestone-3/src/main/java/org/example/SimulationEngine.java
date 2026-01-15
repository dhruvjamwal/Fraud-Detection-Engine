package org.example;

import org.example.model.Transaction;
import org.example.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SimulationEngine implements CommandLineRunner {

    @Autowired
    private FraudDetectionService detectionService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--- STARTING 28-RULE FRAUD SIMULATION ---\n");

        String user = "Dhruv_User";

        System.out.println("[Test 1] Structuring (49,999 - Just below limit)");
        createTxn(user, 49999.00, "Bank Transfer", "FINANCE", "IN");

        System.out.println("\n[Test 2] Buying Chips at Casino");
        createTxn(user, 500.00, "Royal Casino", "GAMBLING", "US");

        System.out.println("\n[Test 3] Using Stolen Device ID");
        Transaction t = new Transaction();
        t.setAccountId(user);
        t.setAmount(200.0);
        t.setDeviceId("STOLEN_DEVICE_ID_99"); // Triggers Rule 28
        detectionService.processTransaction(t);

        System.out.println("\n[Test 4] Velocity Bot Attack");
        createTxn(user, 10.0, "Steam", "GAMES", "IN");
        createTxn(user, 10.0, "Steam", "GAMES", "IN");
        createTxn(user, 10.0, "Steam", "GAMES", "IN");
        createTxn(user, 10.0, "Steam", "GAMES", "IN"); // Fails
    }

    private void createTxn(String user, Double amount, String merchant, String category, String country) {
        Transaction t = new Transaction();
        t.setAccountId(user);
        t.setAmount(amount);
        t.setMerchant(merchant);
        t.setMerchantCategory(category);
        t.setCountry(country);
        t.setType("DEBIT");

        detectionService.processTransaction(t);
    }
}