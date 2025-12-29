package org.example.repository;

import org.example.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp > :startTime")
    long countRecentTransactions(@Param("accountId") String accountId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.accountId = :accountId")
    Double findAverageTransactionAmount(@Param("accountId") String accountId);
}