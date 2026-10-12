package com.rusobr.mail;

import com.rusobr.mail.domain.model.Mail;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailPublisher {

    private final StreamBridge streamBridge;

    public void publishMailEvent(Mail mail, List<Long> recipientIds, String routingKey) {
        MailSentEvent event = new MailSentEvent(
                mail.getId(), mail.getSenderId(), recipientIds, mail.getSubject(), mail.getCreatedAt()
        );

        streamBridge.send("mailSent-out-0",
                MessageBuilder.withPayload(event)
                        .setHeader("amqp_routingKey", "mail.sent." + routingKey)
                        .build());
    }

}
