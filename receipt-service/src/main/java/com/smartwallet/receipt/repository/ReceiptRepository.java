package com.smartwallet.receipt.repository;

import com.smartwallet.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    List<Receipt> findByTransactionId(UUID transactionId);
}
