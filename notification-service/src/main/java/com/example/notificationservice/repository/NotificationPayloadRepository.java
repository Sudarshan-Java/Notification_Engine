package com.example.notificationservice.repository;

import com.example.notificationservice.entity.NotificationPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationPayloadRepository extends JpaRepository<NotificationPayload, Long> {

    List<NotificationPayload> findByUserId(Long userId);

    List<NotificationPayload> findByUserIdAndUserStatus(Long userId, String userStatus);

    @Query("SELECT n FROM NotificationPayload n WHERE (n.status = 'PENDING' OR n.status = 'RETRY') AND n.scheduledDate <= :now")
    List<NotificationPayload> findReadyNotifications(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE NotificationPayload n SET n.status = :newStatus, n.updatedDate = :updated WHERE n.id = :id AND (n.status = :expectedStatus OR (n.status = 'RETRY' AND :expectedStatus = 'RETRY'))")
    int updateStatusIfMatches(@Param("id") Long id, @Param("expectedStatus") String expectedStatus, @Param("newStatus") String newStatus, @Param("updated") LocalDateTime updated);

}
