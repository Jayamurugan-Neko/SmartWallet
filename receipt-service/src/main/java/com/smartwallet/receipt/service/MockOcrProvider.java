package com.smartwallet.receipt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class MockOcrProvider implements OcrProvider {
    
    private final Random random = new Random();
    
    @Override
    public OcrResult extractText(String objectKey) {
        log.info("Simulating OCR processing for object: {}", objectKey);
        try {
            // Simulate processing time
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String[] merchants = {"Amazon", "Uber", "Starbucks", "Walmart", "Local Cafe"};
        String merchant = merchants[random.nextInt(merchants.length)];
        Double amount = Math.round(random.nextDouble() * 100 * 100.0) / 100.0;
        
        return new OcrResult(merchant, amount);
    }
}
