package com.rusobr.user.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "parent"})
@Getter
@Setter
@Builder
@Table(name = "students")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update students set deleted_at = now() where id = ?")
public class Student extends BaseEntity {
    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Parent parent;

    private String studyProfile;

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Student))
            return false;
        return id != null && id.equals(((Student) obj).getId());
    }
}
