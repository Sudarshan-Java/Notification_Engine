package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationPayloadDetailDTO;
import com.example.notificationservice.dto.NotificationRequestDTO;
import com.example.notificationservice.dto.NotificationResponseDTO;
import com.example.notificationservice.service.NotificationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(@RequestBody NotificationRequestDTO dto) {
        NotificationResponseDTO resp = service.create(dto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> get(@PathVariable Long id) {
        NotificationResponseDTO r = service.get(id);
        if (r == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponseDTO> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<NotificationResponseDTO> getUnread(@PathVariable Long userId) {
        return service.getUnreadByUser(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        service.markRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.ok().build();
    }

    // Batch endpoints

    @GetMapping("/ready")
    public List<NotificationResponseDTO> ready() {
        return service.findReadyNotifications(LocalDateTime.now());
    }

    @PutMapping("/{id}/reserve")
    @Transactional
    public ResponseEntity<Void> reserve(@PathVariable Long id) {
        boolean ok = service.reserveForProcessing(id);
        if (ok)
            return ResponseEntity.ok().build();
        return ResponseEntity.status(409).build(); // conflict, already taken
    }

    @GetMapping("/{id}/payload")
    public List<NotificationPayloadDetailDTO> payload(@PathVariable Long id) {
        return service.getPayloadDetails(id);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
            @RequestBody com.example.notificationservice.dto.StatusUpdateDTO dto) {
        service.updateProcessingStatus(id, dto.getStatus(), dto.getRetryIncrement());
        return ResponseEntity.ok().build();
    }
}
