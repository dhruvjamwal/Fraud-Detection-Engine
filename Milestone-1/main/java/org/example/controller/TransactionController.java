package org.example.controller;

import org.example.model.Transaction;
import org.example.service.FraudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private FraudService service;

    @PostMapping("/check")
    public Transaction checkTransaction(@RequestBody Transaction txn) {
        return service.processTransaction(txn);
    }

    @GetMapping("/frauds")
    public List<Transaction> getFrauds() {
        return service.getFraudTransactions();
    }
}
