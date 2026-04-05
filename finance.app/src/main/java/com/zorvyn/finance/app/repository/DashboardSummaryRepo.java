package com.zorvyn.finance.app.repository;

import com.zorvyn.finance.app.dtos.response.CategorySummary;
import com.zorvyn.finance.app.entity.FinancialRecord;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import com.zorvyn.finance.app.projection.FinancialRecordProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardSummaryRepo extends JpaRepository<FinancialRecord, UUID> {

    @Query("SELECT SUM(f.amount) FROM FinancialRecord f WHERE f.createdBy.id = :userId AND f.type = :type")
    BigDecimal sumTotalByType(@Param("userId") UUID userId, @Param("type") TransactionType type);


    @Query("SELECT new com.zorvyn.finance.app.dtos.response.CategorySummary(f.category, SUM(f.amount)) " +
            "FROM FinancialRecord f " +
            "WHERE f.createdBy.id = :userId AND f.type = :expenseType " +
            "GROUP BY f.category")
    List<CategorySummary> getCategoryBreakdown(
            @Param("userId") UUID userId,
            @Param("expenseType") TransactionType expenseType);

    List<FinancialRecordProjection> findTop5ByCreatedByIdOrderByTransactionDateDesc(UUID userId);

    @Query("SELECT SUM(f.amount) FROM FinancialRecord f " +
            "WHERE f.createdBy.id = :userId " +
            "AND f.type = :type " +
            "AND f.transactionDate >= :start " +
            "AND f.transactionDate < :end")
    BigDecimal sumTotalByTypeAndDateRange(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

