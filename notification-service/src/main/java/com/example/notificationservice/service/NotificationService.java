package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationPayloadDetailDTO;
import com.example.notificationservice.dto.NotificationRequestDTO;
import com.example.notificationservice.dto.NotificationResponseDTO;
import com.example.notificationservice.entity.NotificationPayload;
import com.example.notificationservice.entity.detail.NotificationPayloadDetail;
import com.example.notificationservice.mapper.NotificationMapper;
import com.example.notificationservice.repository.NotificationPayloadDetailRepository;
import com.example.notificationservice.repository.NotificationPayloadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationPayloadRepository payloadRepository;
    private final NotificationPayloadDetailRepository detailRepository;
    private final InAppNotificationSender inAppNotificationSender;

    public NotificationService() {
        this.payloadRepository = null;
        this.detailRepository = null;
        this.inAppNotificationSender = null;
    }

    @Autowired
    public NotificationService(NotificationPayloadRepository payloadRepository,
                              NotificationPayloadDetailRepository detailRepository,
                              InAppNotificationSender inAppNotificationSender) {
        this.payloadRepository = payloadRepository;
        this.detailRepository = detailRepository;
        this.inAppNotificationSender = inAppNotificationSender;
    }

    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Notification request cannot be null");
        }

        String mode = dto.getNotificationMode();

        if ("EMAIL".equalsIgnoreCase(mode)
                && (dto.getRecipient() == null || dto.getRecipient().isBlank())) {
            Map<String, String> payload = dto.getPayload();
            String fallbackEmail = null;
            if (payload != null) {
                for (String key : new String[]{"recipient", "recipientEmail", "email", "to"}) {
                    String value = payload.get(key);
                    if (value != null && !value.isBlank()) {
                        fallbackEmail = value;
                        break;
                    }
                }
            }
            if (fallbackEmail == null || fallbackEmail.isBlank()) {
                throw new IllegalArgumentException("Recipient email is required for EMAIL notifications");
            }
            dto.setRecipient(fallbackEmail);
        }

        if ("SMS".equalsIgnoreCase(mode)) {
            String phone = dto.getRecipient();
            if (phone == null || phone.isBlank()) {
                Map<String, String> payload = dto.getPayload();
                if (payload != null) {
                    for (String key : new String[]{"phone", "phoneNumber", "mobile", "mobileNumber", "recipient"}) {
                        String value = payload.get(key);
                        if (value != null && !value.isBlank()) {
                            phone = value;
                            break;
                        }
                    }
                }
            }
            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("SMS notification requires a phone number");
            }
            dto.setRecipient(phone);
        }

        NotificationPayload e = NotificationMapper.toEntity(dto);
        NotificationPayload saved = payloadRepository.save(e);

        Map<String, String> payload = dto.getPayload();
        if (payload != null) {
            for (Map.Entry<String, String> en : payload.entrySet()) {
                NotificationPayloadDetail d = new NotificationPayloadDetail();
                d.setNotificationId(saved.getId());
                d.setPayloadKey(en.getKey());
                d.setPayloadValue(en.getValue());
                d.setCreatedDate(LocalDateTime.now());
                detailRepository.save(d);
            }
        }

        if ("IN_APP".equalsIgnoreCase(mode)) {
            String userId = dto.getUserId() != null ? String.valueOf(dto.getUserId()) : (dto.getRecipient() != null ? dto.getRecipient() : "UNKNOWN");
            String title = payload != null && payload.get("title") != null ? payload.get("title") : "Notification";
            String message = payload != null && payload.get("message") != null ? payload.get("message") : "";
            if (inAppNotificationSender != null) {
                inAppNotificationSender.send(userId, dto.getSource(), dto.getEventType(), title, message);
            }
            saved.setStatus("SENT");
            saved.setUpdatedDate(LocalDateTime.now());
            payloadRepository.save(saved);
        }

        return NotificationMapper.toDTO(saved);
    }

    public NotificationResponseDTO get(Long id) {
        NotificationPayload p = payloadRepository.findById(id).orElse(null);
        if (p == null) return null;
        return NotificationMapper.toDTO(p);
    }

    public List<NotificationResponseDTO> getByUser(Long userId) {
        return payloadRepository.findByUserId(userId).stream().map(NotificationMapper::toDTO).toList();
    }

    public List<NotificationResponseDTO> getUnreadByUser(Long userId) {
        return payloadRepository.findByUserIdAndUserStatus(userId, "UNREAD").stream().map(NotificationMapper::toDTO).toList();
    }

    @Transactional
    public void markRead(Long id) {
        payloadRepository.findById(id).ifPresent(p -> {
            p.setUserStatus("READ");
            p.setUpdatedDate(LocalDateTime.now());
            payloadRepository.save(p);
        });
    }

    @Transactional
    public void cancel(Long id) {
        payloadRepository.findById(id).ifPresent(p -> {
            p.setStatus("CANCELLED");
            p.setUpdatedDate(LocalDateTime.now());
            payloadRepository.save(p);
        });
    }

    // Batch APIs

    public List<NotificationResponseDTO> findReadyNotifications(LocalDateTime now) {
        return payloadRepository.findReadyNotifications(now).stream().map(NotificationMapper::toDTO).toList();
    }

    @Transactional
    public boolean reserveForProcessing(Long id) {
        // try to atomically update status from PENDING or RETRY to PROCESSING
        int updated = payloadRepository.updateStatusIfMatches(id, "PENDING", "PROCESSING", LocalDateTime.now());
        if (updated == 0) {
            updated = payloadRepository.updateStatusIfMatches(id, "RETRY", "PROCESSING", LocalDateTime.now());
        }
        return updated > 0;
    }

    public List<NotificationPayloadDetailDTO> getPayloadDetails(Long id) {
        List<NotificationPayloadDetail> list = detailRepository.findByNotificationId(id);
        return NotificationMapper.toDetailDTOList(list);
    }

    @Transactional
    public void updateProcessingStatus(Long id, String status, Integer retryIncrement) {
        payloadRepository.findById(id).ifPresent(p -> {
            // handle retry increment
            if (retryIncrement != null && retryIncrement > 0) {
                p.setRetryCount(p.getRetryCount() + retryIncrement);
            }

            // if retryCount reached or exceeded maxRetryCount -> FAILED
            if (p.getMaxRetryCount() != null && p.getRetryCount() != null && p.getRetryCount() >= p.getMaxRetryCount()) {
                p.setStatus("FAILED");
            } else {
                p.setStatus(status);
            }

            p.setUpdatedDate(LocalDateTime.now());
            payloadRepository.save(p);
        });
    }
}
