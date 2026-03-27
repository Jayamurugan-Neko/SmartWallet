package com.smartwallet.bill.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String name;
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    // DAYS, MONTHS, YEARS, NONE
    private String recurrencePattern;

    private boolean isPaid;

    @Column(nullable = false)
    private Integer remindDaysBefore;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
