package com.smartwallet.split.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private UUID fromUserId;
    private UUID toUserId;
    private BigDecimal amount;
}
