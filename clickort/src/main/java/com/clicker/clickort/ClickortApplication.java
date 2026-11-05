package com.clicker.clickort;

import com.clicker.clickort.config.LobbyProperties;
import com.clicker.clickort.service.GameService;
import com.clicker.clickort.service.LobbyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties(LobbyProperties.class)
@SpringBootApplication
public class ClickortApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClickortApplication.class, args);
	}

	@Bean
	public CommandLineRunner initRedis(GameService gameService, LobbyService lobbyService) {
		return args -> {
			gameService.resetScores();
			lobbyService.clearReady();
		};
	}

}
