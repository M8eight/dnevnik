package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepository extends JpaRepository<Parent,Long> {
}
