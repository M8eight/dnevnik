package com.rusobr.user.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@Getter
@Setter
@Builder
@Table(name = "teachers")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update teachers set deleted_at = now() where id = ?")
public class Teacher extends BaseEntity {
    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String phoneNumber;

    private String email;

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Teacher))
            return false;
        return id != null && id.equals(((Teacher) obj).getId());
    }
}
