package com.zorvyn.finance.app.dtos.response;

import java.math.BigDecimal;

public record TrendSummary(
        String label,
        BigDecimal amount
) {
}
