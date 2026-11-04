package com.clicker.clickort.service.impl;

import com.clicker.clickort.dto.ClickEvent;
import com.clicker.clickort.service.KafkaMessengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaMessengerServiceImpl implements KafkaMessengerService<String, ClickEvent> {

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    @Value("${topic.name}")
    private String topicName;

    @Override
    public CompletableFuture<SendResult<String, ClickEvent>> send(ClickEvent clickEvent) {
        return kafkaTemplate.send(topicName, clickEvent);
    }
}
