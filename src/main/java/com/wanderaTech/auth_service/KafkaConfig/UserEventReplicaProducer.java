package com.wanderaTech.auth_service.KafkaConfig;

import com.wanderaTech.common_events.UsersEvent.UserCreatedEventReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventReplicaProducer {
    //this publishes kafka event  to  initialize stock of the product created
    private final KafkaTemplate<String, UserCreatedEventReplica> kafkaTemplate;

    public void  sendUserReplica(UserCreatedEventReplica userCreatedEventReplica){
        log.info("Start sending User replica event ");

        Message<UserCreatedEventReplica> message= MessageBuilder
                .withPayload(userCreatedEventReplica)
                .setHeader(KafkaHeaders.TOPIC,"userReplica-topic")
                .build();
        kafkaTemplate.send(message);

        log.info("sent user  info to services based on role provided  {}", userCreatedEventReplica.getLastName());
        kafkaTemplate.flush();
    }
}
