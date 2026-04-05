package com.zorvyn.finance.app.dtos.response;

import com.zorvyn.finance.app.entity.enums.Category;

import java.math.BigDecimal;

public record CategorySummary(
        Category category,
        BigDecimal total
) {
}
