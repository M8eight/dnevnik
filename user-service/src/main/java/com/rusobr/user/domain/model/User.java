package com.rusobr.user.domain.model;

import com.rusobr.user.infrastructure.enums.UserRoles;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String keycloackId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    @ElementCollection(fetch = FetchType.LAZY, targetClass = UserRoles.class)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRoles> roles = new HashSet<>();

    //todo сделать ссылку на ребенка
    private Long childId;

    @Column(updatable = false)
    private Timestamp created_at;

    @Column
    private Timestamp updated_at;
}
