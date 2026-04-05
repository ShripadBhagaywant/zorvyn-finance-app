package com.zorvyn.finance.app.repository;

import com.zorvyn.finance.app.entity.BlackListedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BlackListedTokenRepository extends JpaRepository<BlackListedToken , UUID> {

    boolean existsByJti(String jti);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
