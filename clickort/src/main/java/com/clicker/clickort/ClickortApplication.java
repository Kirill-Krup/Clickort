package com.clicker.clickort;

import com.clicker.clickort.config.LobbyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties(LobbyProperties.class)
@SpringBootApplication
public class ClickortApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClickortApplication.class, args);
	}

}
