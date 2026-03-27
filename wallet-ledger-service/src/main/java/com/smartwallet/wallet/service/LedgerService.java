package com.smartwallet.wallet.service;

import com.smartwallet.common.event.KafkaEvent;
import com.smartwallet.common.exception.BusinessException;
import com.smartwallet.wallet.dto.TransactionRequest;
import com.smartwallet.wallet.entity.LedgerEntry;
import com.smartwallet.wallet.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyService idempotencyService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BigDecimal getBalance(UUID accountId) {
        return ledgerEntryRepository.calculateBalance(accountId);
    }

    @Transactional
    public UUID processTransaction(TransactionRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyService.isProcessed(idempotencyKey)) {
                log.info("Transaction with idempotency key {} was already processed", idempotencyKey);
                throw new BusinessException("DUPLICATE_TRANSACTION", "Transaction already processed");
            }
        }

        BigDecimal balanceInfo = getBalance(request.getFromAccountId());
        if (balanceInfo.compareTo(request.getAmount()) < 0) {
            // Depending on requirements, wallets can have negative balance if it's credit, but let's say normal wallets can't.
            // But double-entry usually allows it or we check an overdraft limit. For now, allow it or just log.
        }

        UUID transactionId = UUID.randomUUID();

        // Debit entry
        LedgerEntry debitEntry = LedgerEntry.builder()
                .transactionId(transactionId)
                .accountId(request.getFromAccountId())
                .amount(request.getAmount().negate())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();

        // Credit entry
        LedgerEntry creditEntry = LedgerEntry.builder()
                .transactionId(transactionId)
                .accountId(request.getToAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();

        ledgerEntryRepository.saveAll(List.of(debitEntry, creditEntry));

        // Publish event
        KafkaEvent<Map<String, Object>> event = KafkaEvent.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("transaction.created")
                .occurredAt(Instant.now())
                .payload(Map.of(
                        "transactionId", transactionId.toString(),
                        "fromAccountId", request.getFromAccountId().toString(),
                        "toAccountId", request.getToAccountId().toString(),
                        "amount", request.getAmount(),
                        "description", request.getDescription(),
                        "category", request.getCategory() != null ? request.getCategory() : ""
                ))
                .build();
        kafkaTemplate.send("wallet-events", transactionId.toString(), event);

        return transactionId;
    }
}
