package com.rusobr.user.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "children"})
@Getter
@Setter
@Builder
@Table(name = "parents")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update parents set deleted_at = now() where id = ?")
public class Parent extends BaseEntity {
    @Id
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @OneToMany(mappedBy = "parent")
    private List<Student> children = new ArrayList<>();

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Parent))
            return false;
        return id != null && id.equals(((Parent) obj).getId());
    }
}
