package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByKeycloack_id(String username);
}
