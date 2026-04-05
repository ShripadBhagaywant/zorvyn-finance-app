package com.zorvyn.finance.app.specification;

import com.zorvyn.finance.app.entity.User;
import com.zorvyn.finance.app.entity.enums.Role;
import com.zorvyn.finance.app.entity.enums.Status;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> email == null ? null : cb.like(root.get("email"), "%" + email + "%");
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(Status status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
