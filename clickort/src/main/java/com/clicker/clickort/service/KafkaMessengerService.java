package com.clicker.clickort.service;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public interface KafkaMessengerService<K, V> {
    CompletableFuture<SendResult<K, V>> send(V v);
}