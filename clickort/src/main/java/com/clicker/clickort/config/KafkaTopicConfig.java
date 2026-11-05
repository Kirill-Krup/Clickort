package com.clicker.clickort.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic executeTopic(@Value("${topic.name:game_topic}") String topicName) {
        return new NewTopic(topicName, 5, (short) 1);
    }
}
