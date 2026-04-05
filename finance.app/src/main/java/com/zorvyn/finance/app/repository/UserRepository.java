package com.zorvyn.finance.app.repository;

import com.zorvyn.finance.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User>{

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deleted = true")
    boolean existsByEmailAndDeletedTrue(@Param("email") String email);

}