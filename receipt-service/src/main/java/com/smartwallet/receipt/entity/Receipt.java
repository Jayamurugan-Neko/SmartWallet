package com.smartwallet.receipt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private UUID transactionId; // Can be linked later

    @Column(nullable = false)
    private String objectKey; // MinIO path

    private String originalFilename;
    private String contentType;

    @Column(nullable = false)
    private String status; // PROCESSING, COMPLETED, FAILED

    // Extracted data (OCR)
    private String merchantName;
    private Double extractedAmount;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
