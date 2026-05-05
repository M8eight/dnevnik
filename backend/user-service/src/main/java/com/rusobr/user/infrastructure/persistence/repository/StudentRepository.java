package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.web.dto.user.UserResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("""
            select new com.rusobr.user.web.dto.user.UserResponse(s.id, u.firstName, u.lastName, u.username, u.keycloakId)
                        from Student s join s.user u where s.id in :studentIds
                            order by u.lastName
    """)
    List<UserResponse> findAllStudentsByIds(@Param("studentIds") Collection<Long> studentIds);

    @EntityGraph(attributePaths = {"user"})
    Optional<Student> findWithUserById(Long userId);
}
