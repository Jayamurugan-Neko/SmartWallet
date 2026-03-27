package com.smartwallet.receipt.service;

public interface OcrProvider {
    OcrResult extractText(String objectKey);
    
    record OcrResult(String merchantName, Double totalAmount) {}
}
