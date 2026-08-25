package com.example.notificationservice.entity.detail;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_payload_details")
public class NotificationPayloadDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;
    private String payloadKey;

    @Lob
    private String payloadValue;

    private LocalDateTime createdDate;

    public NotificationPayloadDetail() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getPayloadKey() {
        return payloadKey;
    }

    public void setPayloadKey(String payloadKey) {
        this.payloadKey = payloadKey;
    }

    public String getPayloadValue() {
        return payloadValue;
    }

    public void setPayloadValue(String payloadValue) {
        this.payloadValue = payloadValue;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
