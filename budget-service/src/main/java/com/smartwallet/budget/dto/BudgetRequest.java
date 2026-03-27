package com.smartwallet.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BudgetRequest {
    @NotNull
    private UUID userId;

    @NotBlank
    private String category;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal limitAmount;

    @NotBlank
    private String monthYear; // Format: YYYY-MM
}
