package com.zorvyn.finance.app.entity;

import com.zorvyn.finance.app.entity.enums.Category;
import com.zorvyn.finance.app.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "financial_records", indexes = {
        @Index(name = "idx_record_user", columnList = "user_id"),
        @Index(name = "idx_record_date", columnList = "transaction_date"),
        @Index(name = "idx_record_type", columnList = "record_type")
})
@SQLDelete(sql = "UPDATE financial_records SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class FinancialRecord extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_USER_RECORDS"))
    private User createdBy;
}
