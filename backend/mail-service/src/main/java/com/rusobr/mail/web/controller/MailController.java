package com.rusobr.mail.web.controller;

import com.rusobr.mail.application.service.MailService;
import com.rusobr.mail.web.dto.DirectMailRequest;
import com.rusobr.mail.web.dto.MailResponse;
import com.rusobr.mail.web.dto.MailWithUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mails")
public class MailController {

    private final MailService mailService;

    @GetMapping("/{mailId}")
    public MailWithUserResponse getById(@PathVariable Long mailId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return mailService.getById(mailId, userId);
    }

    @GetMapping("/inbox")
    public Page<MailWithUserResponse> getInboxByUserId(Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return mailService.getInboxByUserId(userId, pageable);
    }

    @GetMapping("/sent")
    public Page<MailWithUserResponse> getSentByUserId(Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return mailService.getSentByUserId(userId, pageable);
    }

    @PatchMapping("/unread-count")
    public Integer unreadCount(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
       return  mailService.unreadCount(userId);
    }

    @PostMapping("/direct-mails")
    @ResponseStatus(HttpStatus.CREATED)
    public MailResponse send(@RequestBody @Valid DirectMailRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        return mailService.createMail(userId, request);
    }

    @PatchMapping("/{mailRecipientId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@PathVariable Long mailRecipientId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        mailService.read(mailRecipientId, userId);
    }

    @DeleteMapping("/{mailRecipientId}/inbox")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInbox(@PathVariable Long mailRecipientId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        mailService.deleteInbox(mailRecipientId, userId);
    }

    @DeleteMapping("/{mailId}/sent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSent(@PathVariable Long mailId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("user_id");
        mailService.deleteSent(mailId, userId);
    }

}
