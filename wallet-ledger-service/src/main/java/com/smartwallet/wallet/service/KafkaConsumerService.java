package com.smartwallet.wallet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwallet.common.event.KafkaEvent;
import com.smartwallet.wallet.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final LedgerService ledgerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "split-events", groupId = "wallet-group")
    public void handleSplitSettled(String message) {
        try {
            KafkaEvent<Map<String, Object>> event = objectMapper.readValue(message, new TypeReference<>() {});
            if ("split.settled".equals(event.getEventType())) {
                Map<String, Object> payload = event.getPayload();
                
                UUID fromUser = UUID.fromString(payload.get("fromUserId").toString());
                UUID toUser = UUID.fromString(payload.get("toUserId").toString());
                BigDecimal amount = new BigDecimal(payload.get("amount").toString());
                
                TransactionRequest req = new TransactionRequest();
                req.setFromAccountId(fromUser);
                req.setToAccountId(toUser);
                req.setAmount(amount);
                req.setDescription("Settlement via Split service");
                req.setCategory("Transfer");

                // Use event id as idempotency key
                ledgerService.processTransaction(req, event.getEventId());
                log.info("Processed split settlement transaction from event {}", event.getEventId());
            }
        } catch (Exception e) {
            log.error("Failed to process split-events", e);
        }
    }
}
