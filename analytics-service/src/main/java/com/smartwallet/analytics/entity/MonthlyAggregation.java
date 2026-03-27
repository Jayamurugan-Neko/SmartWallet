package com.smartwallet.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monthly_aggregations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAggregation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String monthYear; // YYYY-MM

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @CreationTimestamp
    private Instant createdAt;
}
