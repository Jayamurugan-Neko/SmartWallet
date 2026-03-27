package com.smartwallet.budget.repository;

import com.smartwallet.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByUserIdAndMonthYear(UUID userId, String monthYear);
    Optional<Budget> findByUserIdAndCategoryAndMonthYear(UUID userId, String category, String monthYear);
}
