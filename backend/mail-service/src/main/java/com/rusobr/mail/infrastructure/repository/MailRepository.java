package com.rusobr.mail.infrastructure.repository;

import com.rusobr.mail.domain.model.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {

    @Modifying
    @Query("""
        update Mail m
        set m.senderDeletedAt = current_timestamp()
        where m.id = :mailId
            and m.senderId = :senderId
    """)
    int markDeletedBySender(@Param("mailId") Long mailId, @Param("senderId") Long senderId);

}
