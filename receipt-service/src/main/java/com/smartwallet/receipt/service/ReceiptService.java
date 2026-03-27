package com.smartwallet.receipt.service;

import com.smartwallet.common.event.KafkaEvent;
import com.smartwallet.common.exception.BusinessException;
import com.smartwallet.receipt.entity.Receipt;
import com.smartwallet.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final MinioService minioService;
    private final OcrProvider ocrProvider;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Receipt uploadReceipt(UUID userId, MultipartFile file) {
        String objectKey = minioService.uploadFile(file);

        Receipt receipt = Receipt.builder()
                .userId(userId)
                .objectKey(objectKey)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .status("PROCESSING")
                .build();

        receipt = receiptRepository.save(receipt);

        // Process asynchronously
        processOcrAsync(receipt.getId(), objectKey);

        return receipt;
    }

    @Async
    protected void processOcrAsync(UUID receiptId, String objectKey) {
        log.info("Starting OCR processing for receipt {}", receiptId);
        try {
            OcrProvider.OcrResult result = ocrProvider.extractText(objectKey);

            Receipt receipt = receiptRepository.findById(receiptId)
                    .orElseThrow(() -> new BusinessException("NOT_FOUND", "Receipt not found"));

            receipt.setMerchantName(result.merchantName());
            receipt.setExtractedAmount(result.totalAmount());
            receipt.setStatus("COMPLETED");

            receipt = receiptRepository.save(receipt);

            // Publish event
            KafkaEvent<Map<String, Object>> event = KafkaEvent.<Map<String, Object>>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("receipt.scanned")
                    .occurredAt(Instant.now())
                    .payload(Map.of(
                            "receiptId", receiptId.toString(),
                            "userId", receipt.getUserId().toString(),
                            "merchantName", result.merchantName(),
                            "amount", result.totalAmount()
                    ))
                    .build();
            kafkaTemplate.send("receipt-events", receiptId.toString(), event);
            log.info("Finished OCR processing for receipt {}", receiptId);

        } catch (Exception e) {
            log.error("OCR processing failed for receipt {}", receiptId, e);
            receiptRepository.findById(receiptId).ifPresent(r -> {
                r.setStatus("FAILED");
                receiptRepository.save(r);
            });
        }
    }
}
