package com.smartwallet.bill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class BillRequest {
    @NotNull
    private UUID userId;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate dueDate;

    private String recurrencePattern; // e.g. MONTHS

    @NotNull
    private Integer remindDaysBefore;
}
