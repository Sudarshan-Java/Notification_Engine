package com.example.notificationbatch.provider.impl;

import com.example.notificationbatch.dto.NotificationResponseDTO;
import com.example.notificationbatch.provider.NotificationProvider;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailNotificationProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String getMode() {
        return "EMAIL";
    }

    @Override
    public boolean send(
            NotificationResponseDTO notification,
            Map<String, String> resolvedPayload) {

        try {
            String recipient = notification.getRecipient();
            if (recipient == null || recipient.isBlank()) {
                String[] candidateKeys = {"recipient", "recipientEmail", "email", "to"};
                for (String key : candidateKeys) {
                    String value = resolvedPayload.get(key);
                    if (value != null && !value.isBlank()) {
                        recipient = value;
                        break;
                    }
                }
            }

            if (recipient == null || recipient.isBlank()) {
                System.err.println(
                        "[EMAIL] Recipient email is missing for notification ID: "
                                + notification.getId());
                return false;
            }

            String subject = resolvedPayload.get("title");
            String message = resolvedPayload.get("message");

            if (subject == null || subject.isBlank()) {
                subject = "Notification";
            }

            if (message == null) {
                message = "";
            }

            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setFrom(fromEmail);
            mail.setTo(recipient);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

            System.out.println(
                    "[EMAIL] Successfully sent to: " + recipient);

            return true;

        } catch (Exception e) {
            System.err.println(
                    "[EMAIL] Failed to send notification ID "
                            + notification.getId()
                            + ": "
                            + e.getMessage());

            return false;
        }
    }
}