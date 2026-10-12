package com.rusobr.mail.domain.model;

import com.rusobr.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "mails")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update mails set deleted_at = now() where id = ?")
public class Mail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    private String subject;

    private String body;

    private Instant senderDeletedAt;

}