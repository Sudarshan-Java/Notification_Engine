package com.example.notificationbatch.batch;

import com.example.notificationbatch.dto.NotificationPayloadDetailDTO;
import com.example.notificationbatch.dto.NotificationResponseDTO;
import com.example.notificationbatch.factory.NotificationProviderFactory;
import com.example.notificationbatch.feign.NotificationServiceClient;
import com.example.notificationbatch.placeholder.PlaceholderResolver;
import com.example.notificationbatch.provider.NotificationProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationBatchScheduler {

    private final NotificationServiceClient client;
    private final NotificationProviderFactory providerFactory;
    private final PlaceholderResolver resolver;

    public NotificationBatchScheduler(NotificationServiceClient client, NotificationProviderFactory providerFactory, PlaceholderResolver resolver) {
        this.client = client;
        this.providerFactory = providerFactory;
        this.resolver = resolver;
    }

    @Scheduled(fixedDelayString = "${notification.batch.interval}")
    public void runBatch() {
        System.out.println("[Batch] Trigger at " + LocalDateTime.now());
        List<NotificationResponseDTO> ready = client.getReadyNotifications();
        if (ready == null || ready.isEmpty()) return;

        for (NotificationResponseDTO n : ready) {
            try {
                try {
                    client.reserve(n.getId());
                } catch (Exception ex) {
                    System.out.println("[Batch] Could not reserve id=" + n.getId());
                    continue;
                }

                List<NotificationPayloadDetailDTO> payloadDetails = client.getPayload(n.getId());
                List<Map.Entry<String, String>> entries = payloadDetails.stream()
                        .map(d -> Map.entry(d.getPayloadKey(), d.getPayloadValue()))
                        .collect(Collectors.toList());

                Map<String, String> resolved = new HashMap<>();
                for (Map.Entry<String, String> e : entries) {
                    resolved.put(e.getKey(), e.getValue());
                }

                String titleTemplate = resolved.get("title");
                String messageTemplate = resolved.get("message");
                if (titleTemplate != null) {
                    resolved.put("title", resolver.resolve(titleTemplate, entries));
                }
                if (messageTemplate != null) {
                    resolved.put("message", resolver.resolve(messageTemplate, entries));
                }

                String recipient = n.getRecipient();
                if (recipient == null || recipient.isBlank()) {
                    for (String key : new String[]{"recipient", "recipientEmail", "email", "to"}) {
                        String value = resolved.get(key);
                        if (value != null && !value.isBlank()) {
                            recipient = value;
                            n.setRecipient(recipient);
                            resolved.put("recipient", recipient);
                            break;
                        }
                    }
                }

                NotificationProvider provider = providerFactory.get(n.getNotificationMode());
                if (provider == null) {
                    System.out.println("[Batch] No provider for mode=" + n.getNotificationMode());
                    client.updateStatus(n.getId(), new com.example.notificationbatch.dto.StatusUpdateDTO("FAILED", null));
                    continue;
                }

                boolean sent = provider.send(n, resolved);
                if (sent) {
                    client.updateStatus(n.getId(), new com.example.notificationbatch.dto.StatusUpdateDTO("SENT", null));
                } else {
                    client.updateStatus(n.getId(), new com.example.notificationbatch.dto.StatusUpdateDTO("RETRY", 1));
                }

            } catch (Exception e) {
                System.out.println("[Batch] error processing id=" + n.getId() + " -> " + e.getMessage());
                try {
                    client.updateStatus(n.getId(), new com.example.notificationbatch.dto.StatusUpdateDTO("RETRY", 1));
                } catch (Exception ex) {
                    System.out.println("[Batch] Failed to update status to RETRY for id=" + n.getId() + " -> " + ex.getMessage());
                }
            }
        }
    }
}
