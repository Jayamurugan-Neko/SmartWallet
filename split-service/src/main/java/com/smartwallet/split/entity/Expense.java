package com.smartwallet.split.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "split_expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID paidByUserId;

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    // A simple representation for equal splits.
    // For exact/percentage splits, this would be a separate OneToMany entity.
    @Column(nullable = false)
    private String splitType; // EQUAL

    @CreationTimestamp
    private Instant createdAt;
}
