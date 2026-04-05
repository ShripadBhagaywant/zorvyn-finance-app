package com.zorvyn.finance.app.projection;

import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface FinancialRecordProjection {

    UUID getId();
    BigDecimal getAmount();
    TransactionType getType();
    Category getCategory();
    LocalDateTime getTransactionDate();
    String getDescription();

    UserSummary getCreatedBy();

    interface UserSummary{
        UUID getId();
        String getUserName();
    }

}
