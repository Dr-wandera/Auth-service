package com.wanderaTech.auth_service.KafkaConfig;

import com.wanderaTech.common_events.RegistrationEvent.RegisterNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterNotificationProducer {
    private final KafkaTemplate<String, RegisterNotificationEvent> kafkaTemplate;

    public void  sendRegistrationEvent(RegisterNotificationEvent registerNotificationEvent){
        log.info("Start sending customer  event  to order service ");

        Message<RegisterNotificationEvent> message= MessageBuilder
                .withPayload(registerNotificationEvent)
                .setHeader(KafkaHeaders.TOPIC,"register-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("sent  email to user   {}", registerNotificationEvent.getLastName());
        kafkaTemplate.flush();

    }}
