package com.example.notificationservice.repository;

import com.example.notificationservice.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    List<InAppNotification> findByUserIdOrderByCreatedAtDesc(String userId);
}
