package com.smartwallet.bill.job;

import com.smartwallet.bill.entity.Bill;
import com.smartwallet.bill.repository.BillRepository;
import com.smartwallet.common.event.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillReminderJob {

    private final BillRepository billRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Run every day at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkDueBills() {
        log.info("Starting daily bill reminder job");
        
        // For simplicity, we just check if it's due in 3 days (or dynamically based on remindDaysBefore).
        // Since we can't easily query dynamic column differences in JPQL, we could pull unripe bills and filter,
        // or write a native query. Let's assume we remind 3 days before for all for this example.
        List<Bill> upcomingBills = billRepository.findBillsDueInDays(3);
        
        for (Bill bill : upcomingBills) {
            // Publish bill.due.soon
            KafkaEvent<Map<String, String>> event = KafkaEvent.<Map<String, String>>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("bill.due.soon")
                    .occurredAt(Instant.now())
                    .payload(Map.of(
                            "userId", bill.getUserId().toString(),
                            "billId", bill.getId().toString(),
                            "billName", bill.getName(),
                            "amount", bill.getAmount().toString(),
                            "dueDate", bill.getDueDate().toString(),
                            "message", "Your bill " + bill.getName() + " for " + bill.getAmount() + " is due on " + bill.getDueDate()
                    ))
                    .build();
            kafkaTemplate.send("bill-events", bill.getId().toString(), event);
            log.info("Published bill.due.soon for bill {}", bill.getId());
        }

        // Also check for overdue
        List<Bill> overdueBills = billRepository.findBillsDueInDays(-1);
        for (Bill bill : overdueBills) {
            // Publish bill.overdue
            KafkaEvent<Map<String, String>> event = KafkaEvent.<Map<String, String>>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("bill.overdue")
                    .occurredAt(Instant.now())
                    .payload(Map.of(
                            "userId", bill.getUserId().toString(),
                            "billId", bill.getId().toString(),
                            "billName", bill.getName(),
                            "amount", bill.getAmount().toString(),
                            "dueDate", bill.getDueDate().toString(),
                            "message", "Your bill " + bill.getName() + " is overdue since " + bill.getDueDate()
                    ))
                    .build();
            kafkaTemplate.send("bill-events", bill.getId().toString(), event);
        }
    }
}
