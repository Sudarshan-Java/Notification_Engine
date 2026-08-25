package com.example.notificationservice.mapper;

import com.example.notificationservice.dto.NotificationPayloadDetailDTO;
import com.example.notificationservice.dto.NotificationRequestDTO;
import com.example.notificationservice.dto.NotificationResponseDTO;
import com.example.notificationservice.entity.NotificationPayload;
import com.example.notificationservice.entity.detail.NotificationPayloadDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationMapper {

    public static NotificationPayload toEntity(NotificationRequestDTO dto) {

        NotificationPayload e = new NotificationPayload();

        e.setSource(dto.getSource());
        e.setEventType(dto.getEventType());
        e.setUserId(dto.getUserId());

        // IMPORTANT
        e.setRecipient(dto.getRecipient());

        e.setNotificationMode(dto.getNotificationMode());
        e.setScheduledDate(dto.getScheduledDate());
        e.setPriority(dto.getPriority());
        e.setMaxRetryCount(dto.getMaxRetryCount());

        e.setRetryCount(0);
        e.setStatus("PENDING");
        e.setUserStatus("UNREAD");
        e.setCreatedDate(LocalDateTime.now());
        e.setUpdatedDate(LocalDateTime.now());

        return e;
    }

    public static NotificationResponseDTO toDTO(NotificationPayload e) {

        NotificationResponseDTO d = new NotificationResponseDTO();

        d.setId(e.getId());
        d.setSource(e.getSource());
        d.setEventType(e.getEventType());
        d.setUserId(e.getUserId());

        // IMPORTANT
        d.setRecipient(e.getRecipient());

        d.setNotificationMode(e.getNotificationMode());
        d.setScheduledDate(e.getScheduledDate());
        d.setStatus(e.getStatus());
        d.setPriority(e.getPriority());
        d.setRetryCount(e.getRetryCount());

        return d;
    }

    public static NotificationPayloadDetailDTO toDetailDTO(
            NotificationPayloadDetail d) {

        NotificationPayloadDetailDTO dto = new NotificationPayloadDetailDTO();

        dto.setId(d.getId());
        dto.setNotificationId(d.getNotificationId());
        dto.setPayloadKey(d.getPayloadKey());
        dto.setPayloadValue(d.getPayloadValue());

        return dto;
    }

    public static List<NotificationPayloadDetailDTO> toDetailDTOList(
            List<NotificationPayloadDetail> list) {

        List<NotificationPayloadDetailDTO> out = new ArrayList<>();

        for (NotificationPayloadDetail d : list) {
            out.add(toDetailDTO(d));
        }

        return out;
    }
}