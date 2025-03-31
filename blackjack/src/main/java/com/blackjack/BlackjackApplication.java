package com.blackjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.blackjack.ui.GameUI;

import javafx.application.Application;

@SpringBootApplication
public class BlackjackApplication {
	public static void main(String[] args) {
		System.out.println("RUNNInG");
		SpringApplication.run(BlackjackApplication.class, args);
		Application.launch(GameUI.class);
	}

}
