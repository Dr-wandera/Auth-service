package com.wanderaTech.auth_service.KafkaConfig;

import com.wanderaTech.common_events.UsersEvent.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProducer {
    //publish event  to order service to save User info needed for notification (user replica)
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    public void  sendUserEvent(UserCreatedEvent userCreatedEvent){
        log.info("Start sending customer  event  to order service ");

        Message<UserCreatedEvent> message= MessageBuilder
                .withPayload(userCreatedEvent)
                .setHeader(KafkaHeaders.TOPIC,"customer-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("sent  email and userid  event to order service for user snapshot {}", userCreatedEvent.getUserId());
        kafkaTemplate.flush();

    }
}
