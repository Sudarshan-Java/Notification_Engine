package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationRequestDTO;
import com.example.notificationservice.dto.NotificationResponseDTO;
import com.example.notificationservice.entity.NotificationPayload;
import com.example.notificationservice.entity.detail.NotificationPayloadDetail;
import com.example.notificationservice.repository.NotificationPayloadDetailRepository;
import com.example.notificationservice.repository.NotificationPayloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPayloadRepository payloadRepository;

    @Mock
    private NotificationPayloadDetailRepository detailRepository;

    @Mock
    private InAppNotificationSender inAppNotificationSender;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(payloadRepository, detailRepository, inAppNotificationSender);
    }

    @Test
    void createEmailNotification_keepsExistingEmailFlow() {
        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setSource("SUBSCRIPTION_ENGINE");
        dto.setEventType("SUBSCRIPTION_EXPIRING");
        dto.setUserId(100L);
        dto.setRecipient("user@example.com");
        dto.setNotificationMode("EMAIL");
        dto.setPriority("HIGH");
        dto.setMaxRetryCount(3);
        dto.setPayload(Map.of("title", "Subscription Expiring", "message", "Your Premium subscription expires tomorrow."));

        NotificationPayload saved = new NotificationPayload();
        saved.setId(1L);
        saved.setNotificationMode("EMAIL");
        saved.setRecipient("user@example.com");
        when(payloadRepository.save(any(NotificationPayload.class))).thenReturn(saved);

        NotificationResponseDTO response = service.create(dto);

        assertNotNull(response);
        assertEquals("EMAIL", response.getNotificationMode());
        assertEquals("user@example.com", response.getRecipient());
        verify(payloadRepository, times(1)).save(any(NotificationPayload.class));
        verify(detailRepository, times(2)).save(any(NotificationPayloadDetail.class));
        verifyNoInteractions(inAppNotificationSender);
    }

    @Test
    void createSmsNotification_requiresPhoneNumber() {
        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setSource("SUBSCRIPTION_ENGINE");
        dto.setEventType("SUBSCRIPTION_EXPIRING");
        dto.setUserId(100L);
        dto.setNotificationMode("SMS");
        dto.setPriority("HIGH");
        dto.setMaxRetryCount(3);
        dto.setPayload(Map.of("title", "Subscription Expiring", "message", "Your Premium subscription expires tomorrow."));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertEquals("SMS notification requires a phone number", ex.getMessage());
    }
}
