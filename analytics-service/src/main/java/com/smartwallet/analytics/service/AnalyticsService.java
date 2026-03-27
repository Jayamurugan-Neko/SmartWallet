package com.smartwallet.analytics.service;

import com.smartwallet.analytics.entity.MonthlyAggregation;
import com.smartwallet.analytics.repository.MonthlyAggregationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MonthlyAggregationRepository repository;

    @Transactional
    public void recordTransaction(UUID userId, String monthYear, String category, BigDecimal amount) {
        MonthlyAggregation agg = repository.findByUserIdAndMonthYearAndCategory(userId, monthYear, category)
                .orElseGet(() -> MonthlyAggregation.builder()
                        .userId(userId)
                        .monthYear(monthYear)
                        .category(category)
                        .totalAmount(BigDecimal.ZERO)
                        .build());

        // For analytics, amount is usually absolute spending or income. 
        // Debit entries are negative in ledger. Let's store absolute spending for categories.
        agg.setTotalAmount(agg.getTotalAmount().add(amount.abs()));
        repository.save(agg);
    }

    @Cacheable(value = "analytics-summary", key = "#userId + '-' + #monthYear")
    public List<MonthlyAggregation> getMonthlySummary(UUID userId, String monthYear) {
        return repository.findByUserIdAndMonthYear(userId, monthYear);
    }

    @Cacheable(value = "analytics-forecast", key = "#userId")
    public Map<String, BigDecimal> getForecast(UUID userId) {
        // Simple Holt-Winters exponential smoothing mock implementation
        // Real Holt-Winters requires alpha, beta, gamma smoothing.
        // For simplicity we will do a basic moving average here if data < 12 months, or mock a forecast.

        List<Object[]> totals = repository.findMonthlyTotals(userId);
        if (totals.isEmpty()) {
            return Map.of("NextMonth", BigDecimal.ZERO);
        }

        double sum = 0;
        for (Object[] row : totals) {
            BigDecimal amt = (BigDecimal) row[1];
            sum += amt.doubleValue();
        }
        
        double avg = sum / totals.size();
        // A very trivial forecast = average + 5% inflation factor
        BigDecimal forecast = BigDecimal.valueOf(avg * 1.05);

        return Map.of("NextMonth", forecast);
    }
}
