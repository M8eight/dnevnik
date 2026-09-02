package com.rusobr.user.infrastructure.specification;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.domain.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

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

    public static Specification<User> studentsWithoutParent() {
        return (root, query, cb) -> {
            Subquery<Long> sub = Objects.requireNonNull(query).subquery(Long.class);
            Root<Student> studentRoot = sub.from(Student.class);
            sub.select(studentRoot.get(Student_.id))
                    .where(cb.isNull(studentRoot.get(Student_.parent)));
            return root.get(User_.id).in(sub);
        };
    }

    public static Specification<User> parentsWithoutStudents() {
        return (root, query, cb) -> {
            Subquery<Long> parentSub = Objects.requireNonNull(query).subquery(Long.class);
            Root<Parent> parentRoot = parentSub.from(Parent.class);
            parentSub.select(parentRoot.get(Parent_.id));

            Subquery<Long> studentSub = Objects.requireNonNull(query).subquery(Long.class);
            Root<Student> studentRoot = studentSub.from(Student.class);
            studentSub.select(studentRoot.get(Student_.parent).get(Parent_.id))
                    .where(cb.isNotNull(studentRoot.get(Student_.parent)));

            return cb.and(
                    root.get(User_.id).in(parentSub),
                    cb.not(root.get(User_.id).in(studentSub))
            );
        };
    }

}
