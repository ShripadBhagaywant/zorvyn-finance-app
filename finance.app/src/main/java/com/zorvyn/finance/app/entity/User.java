package com.zorvyn.finance.app.entity;

import com.zorvyn.finance.app.entity.enums.Role;
import com.zorvyn.finance.app.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email",columnList = "user_email"),
        @Index(name = "idx_user_name",columnList = "user_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class User extends BaseEntity implements UserDetails {

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.VIEWER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    // Locking Mechanism
    @Builder.Default
    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Builder.Default
    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    // UserDetails Interface Implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Returns the role prefixed with ROLE_ (Spring Security standard)
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == Status.ACTIVE && !isDeleted();
    }

    // Helper logic for Service Layer
    public boolean isLockExpired() {
        return accountLocked && lockTime != null
                && lockTime.plusMinutes(15).isBefore(LocalDateTime.now());
    }
}