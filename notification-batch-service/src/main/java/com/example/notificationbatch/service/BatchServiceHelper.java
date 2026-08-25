package com.example.notificationbatch.service;

import com.example.notificationbatch.dto.NotificationPayloadDetailDTO;
import com.example.notificationbatch.factory.NotificationProviderFactory;
import com.example.notificationbatch.placeholder.PlaceholderResolver;
import com.example.notificationbatch.provider.NotificationProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BatchServiceHelper {

    private final PlaceholderResolver placeholderResolver;

    public BatchServiceHelper(PlaceholderResolver placeholderResolver) {
        this.placeholderResolver = placeholderResolver;
    }

    public Map<String, String> toPayloadMap(List<NotificationPayloadDetailDTO> payloadDetails) {
        Map<String, String> payload = new HashMap<>();
        if (payloadDetails == null) {
            return payload;
        }

        for (NotificationPayloadDetailDTO detail : payloadDetails) {
            if (detail == null || detail.getPayloadKey() == null) {
                continue;
            }
            payload.put(detail.getPayloadKey(), detail.getPayloadValue());
        }
        return payload;
    }

    public List<Map.Entry<String, String>> toEntries(List<NotificationPayloadDetailDTO> payloadDetails) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        if (payloadDetails == null) {
            return entries;
        }

        for (NotificationPayloadDetailDTO detail : payloadDetails) {
            if (detail == null || detail.getPayloadKey() == null) {
                continue;
            }
            entries.add(Map.entry(detail.getPayloadKey(), detail.getPayloadValue()));
        }
        return entries;
    }

    public Map<String, String> resolvePayload(List<NotificationPayloadDetailDTO> payloadDetails) {
        Map<String, String> payload = toPayloadMap(payloadDetails);
        List<Map.Entry<String, String>> entries = toEntries(payloadDetails);

        String titleTemplate = payload.get("title");
        String messageTemplate = payload.get("message");

        if (titleTemplate != null) {
            payload.put("title", placeholderResolver.resolve(titleTemplate, entries));
        }
        if (messageTemplate != null) {
            payload.put("message", placeholderResolver.resolve(messageTemplate, entries));
        }

        return payload;
    }

    public NotificationProvider resolveProvider(String notificationMode, NotificationProviderFactory factory) {
        if (notificationMode == null || factory == null) {
            return null;
        }
        return factory.get(notificationMode.trim().toUpperCase());
    }

    public String resolveRecipient(Map<String, String> payload, String currentRecipient) {
        if (currentRecipient != null && !currentRecipient.isBlank()) {
            return currentRecipient;
        }
        if (payload == null) {
            return null;
        }

        String[] keys = {"recipient", "recipientEmail", "email", "to"};
        for (String key : keys) {
            String value = payload.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public boolean canRetry(Integer retryCount, Integer maxRetryCount) {
        if (maxRetryCount == null || maxRetryCount <= 0) {
            return true;
        }
        if (retryCount == null) {
            return true;
        }
        return retryCount < maxRetryCount;
    }
}
