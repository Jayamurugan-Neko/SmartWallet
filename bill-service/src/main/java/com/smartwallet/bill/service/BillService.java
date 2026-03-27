package com.smartwallet.bill.service;

import com.smartwallet.bill.entity.Bill;
import com.smartwallet.bill.repository.BillRepository;
import com.smartwallet.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

    private final BillRepository billRepository;

    public Bill createBill(Bill bill) {
        return billRepository.save(bill);
    }

    public List<Bill> getUserBills(UUID userId) {
        return billRepository.findByUserId(userId);
    }

    public Bill updateBill(UUID id, Bill billDetails) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Bill not found"));
        
        bill.setName(billDetails.getName());
        bill.setAmount(billDetails.getAmount());
        bill.setDueDate(billDetails.getDueDate());
        bill.setRecurrencePattern(billDetails.getRecurrencePattern());
        bill.setRemindDaysBefore(billDetails.getRemindDaysBefore());
        bill.setPaid(billDetails.isPaid());

        return billRepository.save(bill);
    }

    public void deleteBill(UUID id) {
        if (!billRepository.existsById(id)) {
            throw new BusinessException("NOT_FOUND", "Bill not found");
        }
        billRepository.deleteById(id);
    }
}
