package com.rusobr.mail.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MailRecipientId implements Serializable {

    @Column(name = "mail_id")
    private Long mailId;

    @Column(name = "recipient_id")
    private Long recipientId;

}
