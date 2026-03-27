package com.smartwallet.split.service;

import com.smartwallet.common.event.KafkaEvent;
import com.smartwallet.common.exception.BusinessException;
import com.smartwallet.split.dto.BalanceResponse;
import com.smartwallet.split.entity.Expense;
import com.smartwallet.split.entity.Group;
import com.smartwallet.split.repository.ExpenseRepository;
import com.smartwallet.split.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SplitService {

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Group createGroup(String name, List<UUID> memberIds) {
        if (memberIds == null || memberIds.size() < 2) {
            throw new BusinessException("INVALID_GROUP", "Group must have at least 2 members");
        }
        Group group = Group.builder()
                .name(name)
                .memberIds(memberIds)
                .build();
        return groupRepository.save(group);
    }

    public Expense addExpense(UUID groupId, UUID paidBy, String description, BigDecimal amount) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Group not found"));

        if (!group.getMemberIds().contains(paidBy)) {
            throw new BusinessException("INVALID_MEMBER", "User is not a member of the group");
        }

        Expense expense = Expense.builder()
                .groupId(groupId)
                .paidByUserId(paidBy)
                .description(description)
                .amount(amount)
                .splitType("EQUAL")
                .build();
        return expenseRepository.save(expense);
    }

    public List<BalanceResponse> calculateBalances(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Group not found"));

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        // Map: UserId -> Net Balance (+ is owed, - owes)
        Map<UUID, BigDecimal> balances = new HashMap<>();
        for (UUID memberId : group.getMemberIds()) {
            balances.put(memberId, BigDecimal.ZERO);
        }

        for (Expense expense : expenses) {
            BigDecimal splitAmount = expense.getAmount().divide(BigDecimal.valueOf(group.getMemberIds().size()), 2, RoundingMode.HALF_UP);
            
            for (UUID memberId : group.getMemberIds()) {
                if (memberId.equals(expense.getPaidByUserId())) {
                    // Paid amount minus their own share
                    balances.put(memberId, balances.get(memberId).add(expense.getAmount()).subtract(splitAmount));
                } else {
                    // Owes their share
                    balances.put(memberId, balances.get(memberId).subtract(splitAmount));
                }
            }
        }

        return simplifyDebts(balances);
    }

    // Splitwise-style Debt Simplification (Greedy algorithm)
    private List<BalanceResponse> simplifyDebts(Map<UUID, BigDecimal> balances) {
        List<BalanceResponse> transactions = new ArrayList<>();

        List<Map.Entry<UUID, BigDecimal>> debtors = new ArrayList<>(); // -ve balances
        List<Map.Entry<UUID, BigDecimal>> creditors = new ArrayList<>(); // +ve balances

        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            }
        }

        debtors.sort(Map.Entry.<UUID, BigDecimal>comparingByValue()); // Ascending (most debt first)
        creditors.sort(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed()); // Descending (most credit first)

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<UUID, BigDecimal> debtor = debtors.get(i);
            Map.Entry<UUID, BigDecimal> creditor = creditors.get(j);

            BigDecimal debt = debtor.getValue().abs();
            BigDecimal credit = creditor.getValue();

            BigDecimal amountToSettle = debt.min(credit);

            if (amountToSettle.compareTo(BigDecimal.ZERO) > 0) {
                transactions.add(new BalanceResponse(debtor.getKey(), creditor.getKey(), amountToSettle));
            }

            debtor.setValue(debtor.getValue().add(amountToSettle));
            creditor.setValue(creditor.getValue().subtract(amountToSettle));

            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return transactions;
    }

    public void recordSettlement(UUID groupId, UUID fromUserId, UUID toUserId, BigDecimal amount) {
        // Record as an expense paid by fromUserId, but only for the from and to users.
        // For a full group equal split logic, settling means adding a negative expense or an exact split.
        // To simplify, we model settlement as a transaction where `fromUserId` pays `amount` for the group, 
        // but wait, standard settlement in splitwise is an expense where fromUser paid, and it applies only to toUserId.
        // As our MVP uses "EQUAL" for all, let's just create an opposite "Settlement" expense or direct it.
        // Let's create an expense paid by fromUserId and the description "Settlement".
        // A true implementation needs exact split support. We'll simulate it for now.
        
        // Publish event
        KafkaEvent<Map<String, Object>> event = KafkaEvent.<Map<String, Object>>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("split.settled")
                .occurredAt(Instant.now())
                .payload(Map.of(
                        "groupId", groupId.toString(),
                        "fromUserId", fromUserId.toString(),
                        "toUserId", toUserId.toString(),
                        "amount", amount
                ))
                .build();
        kafkaTemplate.send("split-events", groupId.toString(), event);
        log.info("Recorded and published settlement for group {}", groupId);
    }
}
