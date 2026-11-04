package com.clicker.clickort.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService gameExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
