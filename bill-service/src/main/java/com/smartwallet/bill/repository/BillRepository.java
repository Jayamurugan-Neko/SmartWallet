package com.smartwallet.bill.repository;

import com.smartwallet.bill.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    List<Bill> findByUserId(UUID userId);

    @Query(value = "SELECT * FROM bills WHERE is_paid = false AND due_date - :days = current_date", nativeQuery = true)
    List<Bill> findBillsDueInDays(int days);
}
