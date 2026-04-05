package com.zorvyn.finance.app.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "blacklisted_tokens", indexes = {
        @Index(name = "idx_token_jti", columnList = "jti"),
        @Index(name = "idx_token_expiry", columnList = "expiry_date")
})
public class BlackListedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
