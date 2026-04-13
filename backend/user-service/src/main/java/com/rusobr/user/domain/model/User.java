package com.rusobr.user.domain.model;

import com.rusobr.user.infrastructure.enums.UserRoles;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;


@Builder
@Entity
@ToString
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update users set deleted_at = now() where id = ?")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String keycloakId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    @ElementCollection(fetch = FetchType.LAZY, targetClass = UserRoles.class)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRoles> roles = new HashSet<>();

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof User))
            return false;
        return id != null && id.equals(((User) obj).getId());
    }
}
