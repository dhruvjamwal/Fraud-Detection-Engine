package org.example.repository;

import org.example.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 1. Find all frauds
    List<Transaction> findByIsFraudTrue();

    // 2. Count transactions for an account in a specific time range (for Velocity Check)
    int countByAccountIdAndTimestampAfter(String accountId, LocalDateTime time);
}