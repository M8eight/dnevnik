package com.rusobr.mail.infrastructure.repository;

import com.rusobr.mail.domain.model.Mail;
import com.rusobr.mail.domain.model.MailRecipient;
import com.rusobr.mail.domain.model.MailRecipientId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailRecipientRepository extends JpaRepository<MailRecipient, MailRecipientId> {

    @Query("""
        select mr
        from MailRecipient mr
        join fetch mr.mail m
        where mr.id.recipientId = :userId
        order by m.createdAt desc
    """)
    Page<MailRecipient> findMailsByUserId(Long userId, Pageable pageable);

    @Query("""
        select m
        from Mail m
        where m.senderId = :userId
            and m.senderDeletedAt is null
        order by m.createdAt desc
    """)
    Page<Mail> findSentByUserId(Long userId, Pageable pageable);

    @Query("""
        select count(*)
        from MailRecipient mr
        where mr.id.recipientId = :userId
            and mr.read_at is null
    """)
    Integer findUnreadCountByUserId(Long userId);

    @Query("""
        select mr
        from MailRecipient mr
        join fetch mr.mail m
        where mr.id.mailId = :mailId
            and mr.id.recipientId = :recipientId
    """)
    Optional<MailRecipient> findByMailIdAndRecipientId(@Param("mailId") Long mailId, @Param("recipientId") Long recipientId);

    @Modifying
    @Query("""
        update MailRecipient mr
        set mr.read_at = current_timestamp()
        where mr.id.recipientId = :recipientId
            and mr.id.mailId = :mailId
    """)
    void readMailRecipient(@Param("mailId") Long mailId, @Param("recipientId") Long recipientId);

    @Modifying
    @Query("""
        update MailRecipient mr
        set mr.deletedAt = current_timestamp()
        where mr.id.mailId = :mailId
            and mr.id.recipientId = :recipientId
    """)
    int markDeletedInbox(@Param("mailId") Long mailId, @Param("recipientId") Long recipientId);

}
