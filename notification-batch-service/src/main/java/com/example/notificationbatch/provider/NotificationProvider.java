package com.example.notificationbatch.provider;

import com.example.notificationbatch.dto.NotificationResponseDTO;
import java.util.Map;

public interface NotificationProvider {
    String getMode();
    boolean send(NotificationResponseDTO notification, Map<String, String> resolvedPayload);
}
