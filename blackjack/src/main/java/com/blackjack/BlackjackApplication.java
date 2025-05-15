package com.blackjack;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.blackjack.ui.GameUI;

import javafx.application.Application;

@SpringBootApplication
public class BlackjackApplication {

	public static void main(String[] args) {
		System.out.println("RUNNInG");
		SpringApplication.run(BlackjackApplication.class, args);
		Application.launch(GameUI.class);
	}
	@Bean
    public CommandLineRunner runFlask() {
        return args -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("python", "flask/main.py");
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            	pb.start();
                System.out.println("Flask application started by Spring Boot.");

            } catch (java.io.IOException e) {
                System.err.println("Error starting Flask application: " + e.getMessage());
            }
        };
    }
 
}
