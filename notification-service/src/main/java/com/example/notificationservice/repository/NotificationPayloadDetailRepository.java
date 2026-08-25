package com.example.notificationservice.repository;

import com.example.notificationservice.entity.detail.NotificationPayloadDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPayloadDetailRepository extends JpaRepository<NotificationPayloadDetail, Long> {
    List<NotificationPayloadDetail> findByNotificationId(Long notificationId);
}
