package com.smartwallet.wallet.controller;

import com.smartwallet.common.model.PagedResponse;
import com.smartwallet.wallet.dto.TransactionRequest;
import com.smartwallet.wallet.entity.LedgerEntry;
import com.smartwallet.wallet.repository.LedgerEntryRepository;
import com.smartwallet.wallet.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final LedgerService ledgerService;
    private final LedgerEntryRepository ledgerEntryRepository;

    @GetMapping("/wallets/{userId}")
    public Map<String, Object> getBalance(@PathVariable UUID userId) {
        BigDecimal balance = ledgerService.getBalance(userId);
        return Map.of("accountId", userId, "balance", balance);
    }

    @PostMapping("/transactions")
    public Map<String, UUID> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        UUID transactionId = ledgerService.processTransaction(request, idempotencyKey);
        return Map.of("transactionId", transactionId);
    }

    @GetMapping("/ledger/{accountId}")
    public PagedResponse<LedgerEntry> getLedger(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerEntry> entries = ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
        
        PagedResponse.PageMetadata metadata = PagedResponse.PageMetadata.builder()
                .number(entries.getNumber())
                .size(entries.getSize())
                .totalElements(entries.getTotalElements())
                .totalPages(entries.getTotalPages())
                .build();
        
        return new PagedResponse<>(entries.getContent(), metadata);
    }
    
    @GetMapping("/transactions")
    public PagedResponse<LedgerEntry> getAllTransactionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            
        // Usually, GET /transactions might be specific to the current user, 
        // extracting checking X-User-Id. Let's do a simple full ledger paginated for now.
        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerEntry> entries = ledgerEntryRepository.findAll(pageable);
        
        PagedResponse.PageMetadata metadata = PagedResponse.PageMetadata.builder()
                .number(entries.getNumber())
                .size(entries.getSize())
                .totalElements(entries.getTotalElements())
                .totalPages(entries.getTotalPages())
                .build();
        
        return new PagedResponse<>(entries.getContent(), metadata);
    }
}
