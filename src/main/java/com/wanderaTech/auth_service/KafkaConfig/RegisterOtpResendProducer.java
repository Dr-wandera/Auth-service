package com.wanderaTech.auth_service.KafkaConfig;

import com.wanderaTech.common_events.RegistrationEvent.RegisterNotificationEvent;
import com.wanderaTech.common_events.RegistrationEvent.RegistrationOtpResendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterOtpResendProducer {
    private final KafkaTemplate<String, RegisterNotificationEvent> kafkaTemplate;

    public void  sendRegisterResendToken(RegistrationOtpResendEvent registrationOtpResendEvent){
        log.info("Start sending resend Otp to notification service ");

        Message<RegistrationOtpResendEvent> message= MessageBuilder
                .withPayload(registrationOtpResendEvent)
                .setHeader(KafkaHeaders.TOPIC,"resendOtp-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("sent  email to user   {}", registrationOtpResendEvent.getLastName());
        kafkaTemplate.flush();

    }
}
