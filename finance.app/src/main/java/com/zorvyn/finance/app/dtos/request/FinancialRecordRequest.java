package com.zorvyn.finance.app.dtos.request;

import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialRecordRequest(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        TransactionType type,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDateTime transactionDate,

        @Size(max = 1000, message = "Description too long")
        String description
) {
}
