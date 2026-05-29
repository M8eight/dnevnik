package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.web.dto.feign.UserResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<Teacher> findWithUserById(Long id);

    @Query(value = "select * from teachers where id = :id", nativeQuery = true)
    Optional<Teacher> findByIdWithDeleted(@Param("id") Long id);

    @Query("""
                    select new com.rusobr.user.web.dto.feign.UserResponse(t.id, u.firstName, u.lastName, u.username, u.keycloakId)
                        from Teacher t
                        join t.user u where t.id in :teacherIds
                        order by u.lastName
            """)
    List<UserResponse> findAllTeachersByIds(@Param("teacherIds") Collection<Long> teacherIds);

    @Query("""
                    select new com.rusobr.user.web.dto.feign.UserResponse(t.id, u.firstName, u.lastName, u.username, u.keycloakId)
                        from Teacher t
                        join t.user u where t.id = :id
                        order by u.lastName
""")
    UserResponse getTeacherSimpleById(Long id);
}
