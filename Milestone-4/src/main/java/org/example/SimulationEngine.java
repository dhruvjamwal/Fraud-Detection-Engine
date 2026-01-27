package org.example;

import org.example.model.Transaction;
import org.example.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class SimulationEngine implements CommandLineRunner {

    @Autowired
    private FraudDetectionService detectionService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--- STARTING HIGH-VOLUME SIMULATION (50+ RECORDS) ---\n");

        String[] users = {"User_Dhruv", "Alice_W", "Bob_M", "Hacker_X", "Merchant_G"};
        String[] merchants = {"Amazon", "Netflix", "CryptoStore", "Apple", "Uber", "Starbucks", "Casino_Royale"};
        String[] countries = {"IN", "US", "CN", "RU", "BR"};
        Random rand = new Random();

        // 1. Generate 50 Random Historical Transactions
        for (int i = 0; i < 55; i++) {
            String user = users[rand.nextInt(users.length)];
            String merch = merchants[rand.nextInt(merchants.length)];
            String country = countries[rand.nextInt(countries.length)];
            double amount = 10 + (rand.nextDouble() * 60000); // Random amount 10 to 60k

            // Generate some fraud scenarios specifically
            String ip = "192.168.1." + rand.nextInt(255);
            String device = "Device_" + rand.nextInt(999);

            createTxn(user, amount, merch, "GENERAL", country, ip, device);

            // Add a small delay so timestamps differ slightly (optional, good for sorting)
            Thread.sleep(10);
        }

        System.out.println("--- DASHBOARD PRE-LOADED WITH 55 TRANSACTIONS ---\n");
    }

    private void createTxn(String user, Double amount, String merchant, String category, String country, String ip, String device) {
        Transaction t = new Transaction();
        t.setAccountId(user);
        t.setAmount(amount);
        t.setMerchant(merchant);
        t.setMerchantCategory(category);
        t.setCountry(country);
        t.setIpAddress(ip);
        t.setDeviceId(device);
        t.setType("DEBIT");

        detectionService.processTransaction(t);
    }
}