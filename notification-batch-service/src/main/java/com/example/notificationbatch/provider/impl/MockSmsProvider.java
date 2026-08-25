package com.example.notificationbatch.provider.impl;

import com.example.notificationbatch.dto.NotificationResponseDTO;
import com.example.notificationbatch.provider.NotificationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockSmsProvider implements NotificationProvider {

    @Value("${notification.sms.enabled:true}")
    private boolean enabled;

    @Value("${notification.sms.provider:mock}")
    private String provider;

    @Override
    public String getMode() {
        return "SMS";
    }

    @Override
    public boolean send(NotificationResponseDTO notification, Map<String, String> resolvedPayload) {
        if (!enabled || !"mock".equalsIgnoreCase(provider)) {
            return false;
        }

        String phoneNumber = notification.getRecipient();
        String message = resolvedPayload != null ? resolvedPayload.get("message") : null;
        if (phoneNumber == null || phoneNumber.isBlank()) {
            System.err.println("[SMS] Recipient phone number is missing");
            return false;
        }
        if (message == null) {
            message = "";
        }

        System.out.println("SMS SENT");
        System.out.println("Recipient: " + phoneNumber);
        System.out.println("Message: " + message);
        return true;
    }
}
