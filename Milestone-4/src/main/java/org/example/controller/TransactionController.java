package org.example.controller;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class TransactionController {

    @Autowired
    private FraudDetectionService detectionService;

    @Autowired
    private TransactionRepository repository;

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        if (transaction.getTimestamp() == null) transaction.setTimestamp(LocalDateTime.now());
        return detectionService.processTransaction(transaction);
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total_fraud", repository.countByIsFraud(true));
        stats.put("total_clean", repository.countByIsFraud(false));
        stats.put("total_txns", repository.count());
        return stats;
    }

    @GetMapping("/history")
    public List<Transaction> getHistory() {
        return repository.findAllByOrderByTimestampDesc();
    }

    @PostMapping("/simulate")
    public Transaction triggerSimulation(@RequestParam String type) {
        Transaction t = new Transaction();
        t.setTimestamp(LocalDateTime.now());
        t.setAccountId("SIM_USER_123");

        t.setIpAddress("192.168.1." + (int)(Math.random() * 255));
        t.setDeviceId("Device_" + (int)(Math.random() * 1000));

        switch (type) {
            case "high_value":
                t.setAmount(60000.00);
                t.setMerchant("Luxury Store");
                t.setCountry("US");
                break;
            case "velocity":
                t.setAmount(10.00);
                t.setMerchant("Test");
                t.setAccountId("USER_99");
                t.setCountry("RU");
                break;
            case "crypto":
                t.setAmount(15000.00);
                t.setMerchant("CryptoStore");
                t.setCountry("CN");
                break;
            case "standard":
            default:
                t.setAmount(10.0 + Math.random() * 500.0);
                t.setMerchant("Retail Shop");
                t.setCountry("IN");
                break;
        }

        return detectionService.processTransaction(t);
    }

    @DeleteMapping("/reset")
    public Map<String, String> resetSystem() {
        repository.deleteAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "System Reset Successful");
        return response;
    }
}
