package com.zorvyn.finance.app.specification;

import com.zorvyn.finance.app.entity.FinancialRecord;
import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class FinancialSpecification {

    public static Specification<FinancialRecord> hasUser(UUID userId){
        return (root, query, cb) -> cb.equal(root.get("createdBy").get("id"),userId);
    }

    public static Specification<FinancialRecord> hasType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> hasCategory(Category category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<FinancialRecord> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) return cb.between(root.get("transactionDate"), start, end);
            return start != null ? cb.greaterThanOrEqualTo(root.get("transactionDate"), start) :
                    cb.lessThanOrEqualTo(root.get("transactionDate"), end);
        };
    }

    public static Specification<FinancialRecord> descriptionContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(
                    cb.lower(root.get("description")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }
}
