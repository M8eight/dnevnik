package com.rusobr.mail.application.mapper;

import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.mail.domain.model.Mail;
import com.rusobr.mail.domain.model.MailRecipient;
import com.rusobr.mail.web.dto.MailResponse;
import com.rusobr.mail.web.dto.MailWithUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MailMapper {

    Mail toEntity(Long senderId, String subject, String body);

    @Mapping(target = "sentAt", source = "createdAt")
    MailResponse toResponse(Mail mail);

    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "mailId", source = "mailRecipient.mail.id")
    @Mapping(target = "sentAt", source = "mailRecipient.mail.createdAt")
    @Mapping(target = "subject", source = "mailRecipient.mail.subject")
    @Mapping(target = "body", source = "mailRecipient.mail.body")
    MailWithUserResponse toWithUserResponse(MailRecipient mailRecipient, UserFeignResponse sender);

    @Mapping(target = "mailId", source = "mail.id")
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "sentAt", source = "mail.createdAt")
    MailWithUserResponse toWithUserResponse(Mail mail, UserFeignResponse sender);

}
