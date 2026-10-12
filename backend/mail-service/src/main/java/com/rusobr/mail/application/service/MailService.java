package com.rusobr.mail.application.service;

import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.mail.MailPublisher;
import com.rusobr.mail.application.mapper.MailMapper;
import com.rusobr.mail.domain.model.Mail;
import com.rusobr.mail.domain.model.MailRecipient;
import com.rusobr.mail.domain.model.MailRecipientId;
import com.rusobr.mail.infrastructure.feign.UserClient;
import com.rusobr.mail.infrastructure.repository.MailRecipientRepository;
import com.rusobr.mail.infrastructure.repository.MailRepository;
import com.rusobr.mail.web.dto.DirectMailRequest;
import com.rusobr.mail.web.dto.MailResponse;
import com.rusobr.mail.web.dto.MailWithUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rusobr.mail.web.exception.MailExceptionCode.MAIL_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailMapper mailMapper;
    private final MailRepository mailRepository;
    private final MailRecipientRepository mailRecipientRepository;
    private final MailPublisher mailPublisher;
    private final UserClient userClient;

    @Lazy
    @Autowired
    private MailService mailService;

    public MailWithUserResponse getById(Long mailId, Long userId) {
        MailRecipient mailRecipient = mailRecipientRepository.findByMailIdAndRecipientId(mailId, userId).orElseThrow(() ->
                new NotFoundException("Mail not found with id:%s".formatted(mailId), MAIL_NOT_FOUND));
        UserFeignResponse sender = userClient.getUserById(mailRecipient.getMail().getSenderId());
        return mailMapper.toWithUserResponse(mailRecipient, sender);
    }

    public Page<MailWithUserResponse> getInboxByUserId(Long userId, Pageable pageable) {
        Page<MailRecipient> mailRecipients = mailRecipientRepository.findMailsByUserId(userId, pageable);
        List<Long> ids = mailRecipients.getContent().stream().map(m -> m.getMail().getSenderId()).toList();
        List<UserFeignResponse> senders = userClient.getBatchUsers(ids).found();
        Map<Long, UserFeignResponse> usersMap = senders.stream().collect(
                Collectors.toMap(UserFeignResponse::id, Function.identity())
        );
        return mailRecipients.map(mailRecipient ->
                mailMapper.toWithUserResponse(mailRecipient, usersMap.get(mailRecipient.getMail().getSenderId())));
    }

    public Page<MailWithUserResponse> getSentByUserId(Long userId, Pageable pageable) {
        Page<Mail> mails = mailRecipientRepository.findSentByUserId(userId, pageable);
        List<Long> ids = mails.getContent().stream().map(Mail::getSenderId).toList();
        List<UserFeignResponse> senders = userClient.getBatchUsers(ids).found();
        Map<Long, UserFeignResponse> usersMap = senders.stream().collect(
                Collectors.toMap(UserFeignResponse::id, Function.identity())
        );
        return mails.map(mail ->
                mailMapper.toWithUserResponse(mail, usersMap.get(mail.getSenderId())));
    }

    public Integer unreadCount(Long userId) {
        return mailRecipientRepository.findUnreadCountByUserId(userId);
    }

    public MailResponse createMail(Long senderId, DirectMailRequest req) {
        userClient.existBatchUsers(req.recipientIds());
        Mail mail = mailService.createMail(senderId, req.subject(), req.body(), req.recipientIds());
        String routingKey = "direct." + req.recipientIds();
        mailPublisher.publishMailEvent(mail, req.recipientIds(), routingKey);
        MailResponse res = mailMapper.toResponse(mail);
        log.info("Mail for user id: {} created: {}", req.recipientIds(), res);
        return res;
    }

    @Transactional
    protected Mail createMail(Long senderId, String subject, String body, List<Long> recipientIds) {
        Mail mail = mailRepository.save(
                mailMapper.toEntity(senderId, subject, body)
        );
        List<MailRecipient> rows = recipientIds.stream().map(recipientId -> {
            MailRecipientId recipientEmbeddable = new MailRecipientId(mail.getId(), recipientId);
            return MailRecipient.builder()
                    .id(recipientEmbeddable)
                    .mail(mail)
                    .build();
        }).toList();
        mailRecipientRepository.saveAll(rows);
        return mail;
    }

    @Transactional
    public void read(Long mailId, Long userId) {
        mailRecipientRepository.readMailRecipient(mailId, userId);
    }

    @Transactional
    public void deleteInbox(Long mailId, Long userId) {
        int updated = mailRecipientRepository.markDeletedInbox(mailId, userId);
        if (updated == 0) {
            throw new NotFoundException("Mail not found with id:%s".formatted(mailId), MAIL_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteSent(Long mailId, Long userId) {
        int updated = mailRepository.markDeletedBySender(mailId, userId);
        if (updated == 0) {
            throw new NotFoundException("Mail not found with id:%s".formatted(mailId), MAIL_NOT_FOUND);
        }
    }

}
