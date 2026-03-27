package com.smartwallet.analytics.controller;

import com.smartwallet.analytics.entity.MonthlyAggregation;
import com.smartwallet.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public List<MonthlyAggregation> getSummary(@RequestParam UUID userId, @RequestParam String monthYear) {
        return analyticsService.getMonthlySummary(userId, monthYear);
    }

    @GetMapping("/categories")
    public List<MonthlyAggregation> getCategories(@RequestParam UUID userId, @RequestParam String monthYear) {
        // Essentially the same as summary but can group logic differently if needed
        return analyticsService.getMonthlySummary(userId, monthYear);
    }

    @GetMapping("/forecast")
    public Map<String, BigDecimal> getForecast(@RequestParam UUID userId) {
        return analyticsService.getForecast(userId);
    }
}
