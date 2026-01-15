package org.example.controller;

import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
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
        return repository.findTop10ByOrderByTimestampDesc();
    }

    @GetMapping("/geo-stats")
    public Map<String, Integer> getGeoStats() {
        List<Transaction> frauds = repository.findAll().stream()
                .filter(Transaction::isFraud)
                .collect(Collectors.toList());

        Map<String, Integer> mapData = new HashMap<>();
        mapData.put("US", 15);
        mapData.put("CN", 30);
        mapData.put("RU", 25);
        mapData.put("BR", 12);
        mapData.put("IN", 5);

        for (Transaction t : frauds) {
            String c = t.getCountry();
            if(c != null) mapData.put(c, mapData.getOrDefault(c, 0) + 1);
        }
        return mapData;
    }

    @PostMapping("/simulate")
    public String triggerSimulation(@RequestParam String type) {
        Transaction t = new Transaction();
        t.setTimestamp(LocalDateTime.now());
        t.setAccountId("TEST_HACKER_FIXED_ID");

        if ("standard".equals(type)) {
            t.setAmount(10.0 + Math.random() * 500.0);
            t.setMerchant("Amazon Retail");
            t.setCountry("IN");
            detectionService.processTransaction(t);
            return "Standard Transaction Generated";
        }

        switch (type) {
            case "high_value":
                t.setAmount(60000.00);
                t.setMerchant("Over");
                t.setType("CREDIT");
                t.setCountry("US");
                break;
            case "velocity":
                t.setAmount(10.00);
                t.setMerchant("Bot Test");
                t.setAccountId("BOT_USER");
                t.setCountry("RU");
                break;
            case "crypto":
                t.setAmount(15000.00);
                t.setMerchant("CryptoStore");
                t.setMerchantCategory("CRYPTO");
                t.setCountry("CN");
                break;
            default:
                t.setAmount(500.00);
                t.setMerchant("General");
                t.setType("DEBIT");
                t.setCountry("IN");
        }

        detectionService.processTransaction(t);
        return "Simulation Triggered: " + type;
    }

    @DeleteMapping("/reset")
    public String resetSystem() {
        repository.deleteAll();
        return "System Reset Successful";
    }
}