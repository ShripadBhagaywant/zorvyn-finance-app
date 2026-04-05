package com.zorvyn.finance.app.service.impl;

import com.zorvyn.finance.app.dtos.response.DashboardResponse;
import com.zorvyn.finance.app.dtos.response.TrendSummary;
import com.zorvyn.finance.app.entity.User;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import com.zorvyn.finance.app.repository.DashboardSummaryRepo;
import com.zorvyn.finance.app.service.DashboardSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardSummaryImpl implements DashboardSummary {

    private final DashboardSummaryRepo dashboardSummary;

    @Override
    public DashboardResponse getDashboardSummary() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = user.getId();

        log.info("Building dashboard summary | userId={}", userId);

        BigDecimal income = Optional.ofNullable(dashboardSummary.sumTotalByType(userId, TransactionType.INCOME))
                .orElse(BigDecimal.ZERO);
        BigDecimal expense = Optional.ofNullable(dashboardSummary.sumTotalByType(userId, TransactionType.EXPENSE))
                .orElse(BigDecimal.ZERO);

        BigDecimal netBalance = income.subtract(expense);

        log.info("Dashboard totals calculated | userId={} totalIncome={} totalExpense={} netBalance={}",
                userId, income, expense, netBalance);

        return DashboardResponse.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .netBalance(netBalance)
                .categoryBreakdown(dashboardSummary.getCategoryBreakdown(userId,TransactionType.INCOME))
                .categoryBreakdown(dashboardSummary.getCategoryBreakdown(userId,TransactionType.EXPENSE))
                .recentActivity(dashboardSummary.findTop5ByCreatedByIdOrderByTransactionDateDesc(userId))
                .weeklyTrends(buildWeeklyTrends(userId))
                .build();
    }


    private List<TrendSummary> buildWeeklyTrends(UUID userId) {
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .toLocalDate()
                .atStartOfDay();

        log.info("Building weekly trends | userId={} weekStart={}", userId, startOfWeek.toLocalDate());

        List<TrendSummary> trends = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDateTime dayStart = startOfWeek.plusDays(i);
            LocalDateTime dayEnd   = dayStart.plusDays(1);

            String dayLabel = dayStart.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            BigDecimal dayIncome = Optional.ofNullable(
                    dashboardSummary.sumTotalByTypeAndDateRange(userId, TransactionType.INCOME, dayStart, dayEnd)
            ).orElse(BigDecimal.ZERO);

            BigDecimal dayExpense = Optional.ofNullable(
                    dashboardSummary.sumTotalByTypeAndDateRange(userId, TransactionType.EXPENSE, dayStart, dayEnd)
            ).orElse(BigDecimal.ZERO);

            BigDecimal net = dayIncome.subtract(dayExpense);

            log.info("Weekly trend | userId={} day={} income={} expense={} net={}",
                    userId, dayLabel, dayIncome, dayExpense, net);

            trends.add(new TrendSummary(dayLabel,net));
        }

        return trends;
    }

}
