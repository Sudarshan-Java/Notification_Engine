package com.example.notificationservice.service;

import com.example.notificationservice.dto.InAppNotificationResponseDTO;
import com.example.notificationservice.entity.InAppNotification;
import com.example.notificationservice.repository.InAppNotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InAppNotificationSender {

    private final InAppNotificationRepository repository;

    public InAppNotificationSender(InAppNotificationRepository repository) {
        this.repository = repository;
    }

    public InAppNotificationResponseDTO send(String userId, String source, String eventType, String title, String message) {
        InAppNotification entity = new InAppNotification();
        entity.setUserId(userId);
        entity.setSource(source);
        entity.setEventType(eventType);
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setRead(false);
        entity.setCreatedAt(LocalDateTime.now());

        InAppNotification saved = repository.save(entity);

        InAppNotificationResponseDTO dto = new InAppNotificationResponseDTO();
        dto.setId(saved.getId());
        dto.setUserId(saved.getUserId());
        dto.setSource(saved.getSource());
        dto.setEventType(saved.getEventType());
        dto.setTitle(saved.getTitle());
        dto.setMessage(saved.getMessage());
        dto.setIsRead(saved.isRead());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    public Optional<InAppNotification> findById(Long id) {
        return repository.findById(id);
    }
}
