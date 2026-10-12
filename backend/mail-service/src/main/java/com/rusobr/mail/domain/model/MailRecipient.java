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
@Table(name = "mail_recipients")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update mail_recipients set deleted_at = now() where id = ?")
public class MailRecipient extends BaseEntity {

    @EmbeddedId
    private MailRecipientId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("mailId")
    @JoinColumn(name = "mail_id")
    private Mail mail;

    private Instant read_at;

}
