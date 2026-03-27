package com.smartwallet.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId; // Groups double-entry records together

    @Column(nullable = false)
    private UUID accountId; // Corresponds to user or system account

    @Column(nullable = false)
    private BigDecimal amount; // Positive = credit, Negative = debit

    private String description;
    private String category;

    @CreationTimestamp
    private Instant createdAt;
}
