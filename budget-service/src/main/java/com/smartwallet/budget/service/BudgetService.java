package com.smartwallet.budget.service;

import com.smartwallet.common.event.KafkaEvent;
import com.smartwallet.budget.entity.Budget;
import com.smartwallet.budget.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Map<Pattern, String> CATEGORY_RULES = Map.of(
            Pattern.compile("(?i).*amazon.*|.*walmart.*|.*target.*"), "Shopping",
            Pattern.compile("(?i).*uber.*|.*lyft.*|.*taxi.*|.*transit.*"), "Transport",
            Pattern.compile("(?i).*starbucks.*|.*restaurant.*|.*cafe.*"), "Dining",
            Pattern.compile("(?i).*netflix.*|.*spotify.*|.*hulu.*"), "Entertainment",
            Pattern.compile("(?i).*kroger.*|.*safeway.*|.*whole foods.*"), "Groceries"
    );

    public String autoCategorise(String merchantName) {
        if (merchantName == null) return "Other";
        for (Map.Entry<Pattern, String> entry : CATEGORY_RULES.entrySet()) {
            if (entry.getKey().matcher(merchantName).matches()) {
                return entry.getValue();
            }
        }
        return "Other";
    }

    @Transactional
    public void processTransaction(UUID userId, BigDecimal amount, String category, String monthYear) {
        // Find budget for this user/category/month
        budgetRepository.findByUserIdAndCategoryAndMonthYear(userId, category, monthYear).ifPresent(budget -> {
            budget.setCurrentSpent(budget.getCurrentSpent().add(amount));

            BigDecimal percentage = budget.getCurrentSpent()
                    .divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0 && !budget.isAlert100Sent()) {
                sendBudgetAlert(userId, budget, "100%");
                budget.setAlert100Sent(true);
            } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0 && !budget.isAlert80Sent()) {
                sendBudgetAlert(userId, budget, "80%");
                budget.setAlert80Sent(true);
            }

            budgetRepository.save(budget);
        });
    }

    private void sendBudgetAlert(UUID userId, Budget budget, String threshold) {
        KafkaEvent<Map<String, String>> event = KafkaEvent.<Map<String, String>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("budget.alert")
                .occurredAt(Instant.now())
                .payload(Map.of(
                        "userId", userId.toString(),
                        "category", budget.getCategory(),
                        "threshold", threshold,
                        "message", "You have crossed " + threshold + " of your " + budget.getCategory() + " budget!"
                ))
                .build();
        kafkaTemplate.send("budget-events", userId.toString(), event);
        log.info("Sent budget alert for user {} category {} threshold {}", userId, budget.getCategory(), threshold);
    }
}
