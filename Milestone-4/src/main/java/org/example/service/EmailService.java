package org.example.service;

import org.example.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendFraudAlert(Transaction t) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("prismaticgaming1.0@gmail.com");
            message.setTo("prismaticgaming1.0@gmail.com");
            message.setSubject("FRAUD DETECTED: Account " + t.getAccountId());
            message.setText("Security Alert:\n\nTransaction #" + t.getId() + " was blocked.\nAmount: " + t.getAmount() + "\nReason: " + t.getFraudReason());

            mailSender.send(message);
            System.out.println("EMAIL SENT SUCCESSFULLY");
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }
}