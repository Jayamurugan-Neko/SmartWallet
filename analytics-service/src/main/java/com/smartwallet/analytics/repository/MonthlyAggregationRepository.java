package com.smartwallet.analytics.repository;

import com.smartwallet.analytics.entity.MonthlyAggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyAggregationRepository extends JpaRepository<MonthlyAggregation, UUID> {
    Optional<MonthlyAggregation> findByUserIdAndMonthYearAndCategory(UUID userId, String monthYear, String category);

    List<MonthlyAggregation> findByUserIdAndMonthYear(UUID userId, String monthYear);

    @Query("SELECT m.monthYear, SUM(m.totalAmount) FROM MonthlyAggregation m WHERE m.userId = :userId GROUP BY m.monthYear ORDER BY m.monthYear ASC")
    List<Object[]> findMonthlyTotals(@Param("userId") UUID userId);
}
