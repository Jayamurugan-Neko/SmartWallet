package com.smartwallet.split.controller;

import com.smartwallet.split.dto.BalanceResponse;
import com.smartwallet.split.dto.ExpenseRequest;
import com.smartwallet.split.dto.GroupRequest;
import com.smartwallet.split.dto.SettlementRequest;
import com.smartwallet.split.entity.Expense;
import com.smartwallet.split.entity.Group;
import com.smartwallet.split.service.SplitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class SplitController {

    private final SplitService splitService;

    @PostMapping
    public Group createGroup(@Valid @RequestBody GroupRequest request) {
        return splitService.createGroup(request.getName(), request.getMemberIds());
    }

    @PostMapping("/{id}/expenses")
    public Expense addExpense(@PathVariable UUID id, @Valid @RequestBody ExpenseRequest request) {
        return splitService.addExpense(id, request.getPaidByUserId(), request.getDescription(), request.getAmount());
    }

    @GetMapping("/{id}/balances")
    public List<BalanceResponse> getBalances(@PathVariable UUID id) {
        return splitService.calculateBalances(id);
    }

    @PostMapping("/settlements")
    public void recordSettlement(@Valid @RequestBody SettlementRequest request) {
        splitService.recordSettlement(request.getGroupId(), request.getFromUserId(), request.getToUserId(), request.getAmount());
    }
}
