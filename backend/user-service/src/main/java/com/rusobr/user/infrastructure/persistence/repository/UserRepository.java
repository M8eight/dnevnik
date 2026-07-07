package com.rusobr.user.infrastructure.persistence.repository;

import com.rusobr.user.domain.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesById(Long id);

    @Modifying
    @Query("update User u set u.keycloakId = :kId where u.id = :userId")
    void setKeycloakId(@Param("kId") String kId, @Param("userId") Long userId);

}
