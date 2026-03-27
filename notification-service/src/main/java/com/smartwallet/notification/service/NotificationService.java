package com.smartwallet.notification.service;

import com.smartwallet.notification.entity.Notification;
import com.smartwallet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    public void sendNotification(UUID userId, String type, String title, String message, String emailFallback) {
        // Save to DB
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        notification = notificationRepository.save(notification);

        // Push via WebSocket
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", notification);

        // Send Email using MailHog
        if (emailFallback != null) {
            emailService.sendEmail(emailFallback, title, message);
        }
    }
}
