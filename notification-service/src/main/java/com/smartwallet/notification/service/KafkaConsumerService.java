package com.smartwallet.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwallet.common.event.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "bill-events", groupId = "notification-group")
    public void handleBillEvents(String message) {
        processEvent(message, "bill.due.soon", "Bill Reminder");
        processEvent(message, "bill.overdue", "Bill Overdue");
    }

    @KafkaListener(topics = "budget-events", groupId = "notification-group")
    public void handleBudgetEvents(String message) {
        processEvent(message, "budget.alert", "Budget Alerting");
    }

    @KafkaListener(topics = "split-events", groupId = "notification-group")
    public void handleSplitEvents(String message) {
        try {
            KafkaEvent<Map<String, Object>> event = objectMapper.readValue(message, new TypeReference<>() {});
            if ("split.settled".equals(event.getEventType())) {
                Map<String, Object> payload = event.getPayload();
                UUID toUserId = UUID.fromString(payload.get("toUserId").toString());
                String amount = payload.get("amount").toString();
                
                notificationService.sendNotification(
                        toUserId, 
                        "SETTLEMENT", 
                        "Debt Settled", 
                        "You received a settlement of " + amount, 
                        "user-" + toUserId + "@example.com"
                );
            }
        } catch (Exception e) {
            log.error("Failed to process split-event", e);
        }
    }

    private void processEvent(String messageJson, String targetEventType, String title) {
        try {
            KafkaEvent<Map<String, String>> event = objectMapper.readValue(messageJson, new TypeReference<>() {});
            if (targetEventType.equals(event.getEventType())) {
                Map<String, String> payload = event.getPayload();
                UUID userId = UUID.fromString(payload.get("userId"));
                String msgText = payload.get("message");
                
                notificationService.sendNotification(
                        userId, 
                        "ALERT", 
                        title, 
                        msgText, 
                        "user-" + userId + "@example.com"
                );
            }
        } catch (Exception e) {
            log.error("Failed to process event for {}", targetEventType, e);
        }
    }
}
