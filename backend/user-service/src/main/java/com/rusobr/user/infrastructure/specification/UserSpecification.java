package com.rusobr.user.infrastructure.specification;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.domain.model.User_;
import com.rusobr.user.infrastructure.enums.UserRole;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> findByRole(UserRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }

            Join<User, UserRole> joinRoles = root.join(User_.roles, JoinType.LEFT);
            return cb.equal(joinRoles, role);
        };
    }

    public static Specification<User> findByFullNameFuzzy(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) {
                return null;
            }

            //Склеиваем имя и фамилию в одну строку так как в индексе происходит также
            Expression<String> fullName = cb.concat(
                    cb.concat(root.get(User_.firstName), " "),
                    root.get(User_.lastName)
            );

            String pattern = "%" + term + "%";

            return cb.like(cb.lower(fullName), pattern.toLowerCase());

        };
    }
}
