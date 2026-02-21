package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloackId(String keycloackId);
}
