package com.smartwallet.receipt.controller;

import com.smartwallet.common.exception.BusinessException;
import com.smartwallet.common.security.UserContext;
import com.smartwallet.receipt.entity.Receipt;
import com.smartwallet.receipt.repository.ReceiptRepository;
import com.smartwallet.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ReceiptRepository receiptRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Receipt uploadReceipt(@RequestParam("file") MultipartFile file) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            // Provide a dummy UUID if headers aren't injected properly in local standalone testing
            userId = UUID.randomUUID().toString();
        }
        return receiptService.uploadReceipt(UUID.fromString(userId), file);
    }

    @GetMapping("/{id}")
    public Receipt getReceipt(@PathVariable UUID id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Receipt not found"));
    }

    @GetMapping
    public List<Receipt> getReceiptsByTransaction(@RequestParam UUID transactionId) {
        return receiptRepository.findByTransactionId(transactionId);
    }
}
