package com.example.notificationservice.service;

import com.example.notificationservice.dto.InAppNotificationResponseDTO;
import com.example.notificationservice.entity.InAppNotification;
import com.example.notificationservice.repository.InAppNotificationRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InAppNotificationSenderTest {

    @Test
    void send_persistsUnreadNotification() {
        InAppNotificationRepository repository = mock(InAppNotificationRepository.class);
        InAppNotificationSender sender = new InAppNotificationSender(repository);

        InAppNotification saved = new InAppNotification();
        saved.setId(101L);
        saved.setUserId("USER1001");
        saved.setSource("SUBSCRIPTION_ENGINE");
        saved.setEventType("SUBSCRIPTION_PLAN_UPGRADED");
        saved.setTitle("Plan Upgraded");
        saved.setMessage("Your Premium subscription has been upgraded successfully.");
        saved.setRead(false);

        when(repository.save(any(InAppNotification.class))).thenReturn(saved);

        InAppNotificationResponseDTO response = sender.send(
                "USER1001",
                "SUBSCRIPTION_ENGINE",
                "SUBSCRIPTION_PLAN_UPGRADED",
                "Plan Upgraded",
                "Your Premium subscription has been upgraded successfully."
        );

        assertNotNull(response);
        assertEquals("USER1001", response.getUserId());
        assertEquals("Plan Upgraded", response.getTitle());
        assertFalse(response.getIsRead());
        verify(repository).save(any(InAppNotification.class));
    }
}
