package org.example.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountId;
    private Double amount;
    private String merchant;     
    private String type;
    private boolean isFraud;
    private String fraudReason;  
    private LocalDateTime timestamp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isFraud() { return isFraud; }
    public void setFraud(boolean fraud) { isFraud = fraud; }

    public String getFraudReason() { return fraudReason; }
    public void setFraudReason(String fraudReason) { this.fraudReason = fraudReason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
