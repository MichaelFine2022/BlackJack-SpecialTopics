package com.blackjack.ui;

import com.blackjack.game.GameLogic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameUI extends Application {
    private GameLogic game;

    @Override
    public void start(Stage primaryStage) {
        game = new GameLogic();
        game.startGame();

        Label playerHandLabel = new Label("Your Hand: " + game.getPlayerHand());
        Label dealerHandLabel = new Label("Dealer's Hand: " + game.getDealerHand());

        Button hitButton = new Button("Hit");
        Button standButton = new Button("Stand");

        hitButton.setOnAction(e -> {
            game.getPlayerHand().add(game.drawCard());
            playerHandLabel.setText("Your Hand: " + game.getPlayerHand());
        });

        VBox layout = new VBox(10, playerHandLabel, dealerHandLabel, hitButton, standButton);
        Scene scene = new Scene(layout, 300, 200);

        primaryStage.setTitle("Blackjack Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
