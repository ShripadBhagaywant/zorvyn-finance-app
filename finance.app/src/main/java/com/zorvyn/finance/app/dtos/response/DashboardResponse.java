package com.zorvyn.finance.app.dtos.response;

import com.zorvyn.finance.app.projection.FinancialRecordProjection;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DashboardResponse(

        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategorySummary> categoryBreakdown,
        List<FinancialRecordProjection> recentActivity,
        List<TrendSummary> weeklyTrends

) {
}
