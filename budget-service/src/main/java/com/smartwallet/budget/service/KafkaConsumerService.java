package com.smartwallet.budget.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwallet.common.event.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final BudgetService budgetService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
            .withZone(ZoneId.systemDefault());

    @KafkaListener(topics = "wallet-events", groupId = "budget-group")
    public void handleTransactionCreated(String message) {
        try {
            KafkaEvent<Map<String, Object>> event = objectMapper.readValue(message, new TypeReference<>() {});
            if ("transaction.created".equals(event.getEventType())) {
                Map<String, Object> payload = event.getPayload();
                
                // Debit implies spending
                UUID fromUserId = UUID.fromString(payload.get("fromAccountId").toString());
                BigDecimal amount = new BigDecimal(payload.get("amount").toString());
                String category = (String) payload.get("category");
                
                if (category == null || category.isBlank()) {
                    category = budgetService.autoCategorise((String) payload.get("description"));
                }
                
                String monthYear = yearMonthFormatter.format(event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now());
                
                budgetService.processTransaction(fromUserId, amount, category, monthYear);
                log.info("Processed spending for budget. Category: {}, Amount: {}", category, amount);
            }
        } catch (Exception e) {
            log.error("Failed to process wallet-events", e);
        }
    }
}
