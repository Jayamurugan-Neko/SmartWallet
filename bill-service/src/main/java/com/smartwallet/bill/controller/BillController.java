package com.smartwallet.bill.controller;

import com.smartwallet.bill.dto.BillRequest;
import com.smartwallet.bill.entity.Bill;
import com.smartwallet.bill.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping("/{userId}")
    public List<Bill> getUserBills(@PathVariable UUID userId) {
        return billService.getUserBills(userId);
    }

    @PostMapping
    public Bill createBill(@Valid @RequestBody BillRequest request) {
        Bill bill = Bill.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .recurrencePattern(request.getRecurrencePattern())
                .remindDaysBefore(request.getRemindDaysBefore())
                .isPaid(false)
                .build();
        return billService.createBill(bill);
    }

    @PutMapping("/{id}")
    public Bill updateBill(@PathVariable UUID id, @Valid @RequestBody BillRequest request) {
        Bill bill = Bill.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .recurrencePattern(request.getRecurrencePattern())
                .remindDaysBefore(request.getRemindDaysBefore())
                // In a real app we might pass isPaid explicitly in the update request
                .build();
        return billService.updateBill(id, bill);
    }

    @DeleteMapping("/{id}")
    public void deleteBill(@PathVariable UUID id) {
        billService.deleteBill(id);
    }
}
