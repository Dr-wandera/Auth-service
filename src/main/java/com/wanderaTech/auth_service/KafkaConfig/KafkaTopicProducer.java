package com.wanderaTech.auth_service.KafkaConfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicProducer {

    //this  creates customer topic in kafka
    @Bean
    public NewTopic customerTopic(){
        return TopicBuilder
                .name("customer-topic")
                .partitions(1)
                .replicas(1)
                .build();

    }
    //Create registration topic to notify user after success creation of account
    @Bean
    public NewTopic registrationTopic(){
        return TopicBuilder
                .name("register-topic")
                .partitions(1)
                .replicas(1)
                .build();

    }

}
