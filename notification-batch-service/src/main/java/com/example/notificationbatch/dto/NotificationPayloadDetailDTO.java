package com.example.notificationbatch.dto;

public class NotificationPayloadDetailDTO {
    private Long id;
    private Long notificationId;
    private String payloadKey;
    private String payloadValue;

    public NotificationPayloadDetailDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public String getPayloadKey() { return payloadKey; }
    public void setPayloadKey(String payloadKey) { this.payloadKey = payloadKey; }
    public String getPayloadValue() { return payloadValue; }
    public void setPayloadValue(String payloadValue) { this.payloadValue = payloadValue; }
}
