package com.smartwallet.budget.controller;

import com.smartwallet.budget.dto.BudgetRequest;
import com.smartwallet.budget.dto.CategoriseRequest;
import com.smartwallet.budget.dto.CategoriseResponse;
import com.smartwallet.budget.entity.Budget;
import com.smartwallet.budget.repository.BudgetRepository;
import com.smartwallet.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;

    @GetMapping("/categories")
    public List<String> getCategories() {
        return List.of("Shopping", "Transport", "Dining", "Entertainment", "Groceries", "Other", "Bills", "Transfer");
    }

    @PostMapping("/categorise")
    public CategoriseResponse categorise(@Valid @RequestBody CategoriseRequest request) {
        String category = budgetService.autoCategorise(request.getMerchantName());
        return new CategoriseResponse(category);
    }

    @PostMapping("/budgets")
    public Map<String, UUID> createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        Budget budget = budgetRepository.findByUserIdAndCategoryAndMonthYear(
                request.getUserId(), request.getCategory(), request.getMonthYear()
        ).orElseGet(() -> Budget.builder()
                .userId(request.getUserId())
                .category(request.getCategory())
                .monthYear(request.getMonthYear())
                .currentSpent(BigDecimal.ZERO)
                .alert80Sent(false)
                .alert100Sent(false)
                .build());

        budget.setLimitAmount(request.getLimitAmount());
        
        // Reset alerts if limits was increased
        if (budget.getCurrentSpent().compareTo(budget.getLimitAmount()) < 0) {
            budget.setAlert100Sent(false);
            if (budget.getCurrentSpent().compareTo(budget.getLimitAmount().multiply(new BigDecimal("0.8"))) < 0) {
                budget.setAlert80Sent(false);
            }
        }
        
        budget = budgetRepository.save(budget);
        return Map.of("budgetId", budget.getId());
    }

    @GetMapping("/budgets/{userId}")
    public List<Budget> getUserBudgets(@PathVariable UUID userId, @RequestParam String monthYear) {
        return budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
    }
}
