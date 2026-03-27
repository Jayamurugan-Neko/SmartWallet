package com.smartwallet.split.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GroupRequest {
    @NotBlank
    private String name;

    @NotEmpty
    private List<UUID> memberIds;
}
