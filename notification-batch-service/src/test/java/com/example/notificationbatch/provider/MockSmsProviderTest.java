package com.example.notificationbatch.provider;

import com.example.notificationbatch.dto.NotificationResponseDTO;
import com.example.notificationbatch.provider.impl.MockSmsProvider;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MockSmsProviderTest {

    @Test
    void sendSms_logsPhoneNumberAndMessage() {
        MockSmsProvider provider = new MockSmsProvider();
        NotificationResponseDTO notification = new NotificationResponseDTO();
        notification.setId(8L);
        notification.setRecipient("+919876543210");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            boolean sent = provider.send(notification, Map.of("message", "Your Premium subscription expires tomorrow."));
            assertTrue(sent);
        } finally {
            System.setOut(originalOut);
        }

        String log = output.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("SMS SENT"));
        assertTrue(log.contains("+919876543210"));
        assertTrue(log.contains("Your Premium subscription expires tomorrow."));
    }
}
