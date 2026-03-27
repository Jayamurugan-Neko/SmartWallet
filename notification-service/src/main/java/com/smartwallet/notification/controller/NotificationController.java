package com.smartwallet.notification.controller;

import com.smartwallet.common.exception.BusinessException;
import com.smartwallet.notification.entity.Notification;
import com.smartwallet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> getUserNotifications(@RequestParam UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
