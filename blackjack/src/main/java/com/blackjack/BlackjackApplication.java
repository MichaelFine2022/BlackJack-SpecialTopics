package com.blackjack;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.blackjack.ui.Login;

import javafx.application.Application;

@SpringBootApplication
public class BlackjackApplication {

	public static void main(String[] args) {
		System.out.println("RUNNInG");
		SpringApplication.run(BlackjackApplication.class, args);
		Application.launch(Login.class);
	}
	@Bean
    public CommandLineRunner runFlask() {
        return args -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("main", "flask");
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            	pb.start();

            } catch (java.io.IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        };
    }
 
}
