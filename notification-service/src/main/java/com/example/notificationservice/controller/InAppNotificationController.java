package com.example.notificationservice.controller;

import com.example.notificationservice.dto.InAppNotificationResponseDTO;
import com.example.notificationservice.entity.InAppNotification;
import com.example.notificationservice.repository.InAppNotificationRepository;
import com.example.notificationservice.service.InAppNotificationSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class InAppNotificationController {

    private final InAppNotificationRepository repository;
    private final InAppNotificationSender sender;

    public InAppNotificationController(InAppNotificationRepository repository, InAppNotificationSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    @GetMapping("/in-app/{userId}")
    public List<InAppNotificationResponseDTO> getByUser(@PathVariable String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    @PutMapping("/in-app/{notificationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long notificationId) {
        InAppNotification p = repository.findById(notificationId).orElse(null);
        if (p == null) {
            return ResponseEntity.notFound().build();
        }
        p.setRead(true);
        p.setReadAt(LocalDateTime.now());
        repository.save(p);
        return ResponseEntity.ok().build();
    }

    private InAppNotificationResponseDTO toDto(InAppNotification entity) {
        InAppNotificationResponseDTO dto = new InAppNotificationResponseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setSource(entity.getSource());
        dto.setEventType(entity.getEventType());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setIsRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
