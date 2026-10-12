package com.rusobr.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MailNotificationConsumer {

    @Bean
    public Consumer<MailSentEvent> mailNotification() {
        return event -> {
            log.info("Обработка события: письмо {} для {}", event.mailId(), event.recipientId());
        };
    }

}
