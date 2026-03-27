package com.smartwallet.split.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SettlementRequest {
    @NotNull
    private UUID groupId;
    
    @NotNull
    private UUID fromUserId;

    @NotNull
    private UUID toUserId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
