package com.smartwallet.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String title;
    
    @Column(length = 1000)
    private String message;

    private String type; // ALERT, REMINDER, SETTLEMENT

    private boolean isRead;

    @CreationTimestamp
    private Instant createdAt;
}
