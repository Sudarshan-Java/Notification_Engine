package com.example.notificationbatch.feign;

import com.example.notificationbatch.dto.NotificationPayloadDetailDTO;
import com.example.notificationbatch.dto.NotificationResponseDTO;
import com.example.notificationbatch.dto.StatusUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "notification-service-client", url = "${notification.service.url}")
public interface NotificationServiceClient {

    @GetMapping("/api/notifications/ready")
    List<NotificationResponseDTO> getReadyNotifications();

    @PutMapping("/api/notifications/{id}/reserve")
    void reserve(@PathVariable("id") Long id);

    @GetMapping("/api/notifications/{id}/payload")
    List<NotificationPayloadDetailDTO> getPayload(@PathVariable("id") Long id);

    @PutMapping("/api/notifications/{id}/status")
    void updateStatus(@PathVariable("id") Long id, @RequestBody StatusUpdateDTO dto);
}
