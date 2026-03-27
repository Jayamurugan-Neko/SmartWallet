package com.smartwallet.budget.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoriseRequest {
    @NotBlank
    private String merchantName;
}
