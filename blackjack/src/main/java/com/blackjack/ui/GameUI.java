package com.blackjack.ui;

import java.util.List;

import com.blackjack.game.Card;
import com.blackjack.game.GameLogic;
import com.blackjack.game.Player;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class GameUI extends Application {
    private GameLogic game;
    public static Player player;
    private HBox dealerCardDisplay;
    private HBox playerCardDisplay;
    private Label playerScoreLabel;
    private Label dealerScoreLabel;
    private Button hitButton;
    private Button standButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setFullScreen(true);
        player = new Player("currentGame");
        game = new GameLogic(player);
        game.startGame();

        GridPane gameui = new GridPane();
        gameui.setStyle("-fx-background-color: #eeeeee;");
        javafx.scene.layout.RowConstraints row1 = new javafx.scene.layout.RowConstraints();
        javafx.scene.layout.RowConstraints row2 = new javafx.scene.layout.RowConstraints();
        row1.setVgrow(javafx.scene.layout.Priority.ALWAYS);
        gameui.getRowConstraints().addAll(row1, row2);

        hitButton = new Button("Hit");
        hitButton.setPrefWidth(100.00);
        hitButton.setPrefHeight(50.00);
        hitButton.setDisable(false);
        hitButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;");
        hitButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            hitButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null)));
        });
        hitButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            hitButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null)));
        });
        hitButton.setOnAction(event -> {
            Card newCard = game.playerHit();
            displayPlayerCard(newCard);
            updateScores();
            if (game.calculateHandValue(game.getPlayerHand()) > 21) {
                hitButton.setDisable(true);
                standButton.setDisable(true);
                System.out.println("Player Busts!");
            }
        });

        standButton = new Button("Stand");
        standButton.setPrefWidth(100.00);
        standButton.setPrefHeight(50.00);
        standButton.setDisable(false);
        standButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            standButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null)));
        });
        standButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            standButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null)));
        });
        standButton.setOnAction(event -> {
            hitButton.setDisable(true);
            standButton.setDisable(true);
            // Dealer turn
            @SuppressWarnings("unused")
            List<Card> dealerHand = game.dealerPlay();
            displayDealerHand();
            updateScores();
            String result = game.determineWinner();
            System.out.println(result);
        });

        dealerCardDisplay = new HBox(5);
        playerCardDisplay = new HBox(5);
        VBox buttons = new VBox(5, hitButton, standButton);
        playerScoreLabel = new Label("Player: 0");
        dealerScoreLabel = new Label("Dealer: 0");
        VBox playerInfo = new VBox(5, buttons, playerScoreLabel);
        VBox dealerInfo = new VBox(5, dealerScoreLabel);

        HBox playerUI = new HBox(10, playerInfo, playerCardDisplay);
        HBox dealerUI = new HBox(10, dealerInfo, dealerCardDisplay);

        gameui.add(dealerUI, 0, 0);
        GridPane.setMargin(dealerUI, new Insets(10));
        GridPane.setHalignment(dealerUI, HPos.CENTER); 

        gameui.add(playerUI, 0, 1);
        GridPane.setMargin(playerUI, new Insets(10));
        GridPane.setHalignment(playerUI, HPos.CENTER);
        displayInitialHands();
        updateScores();

        Scene scene = new Scene(gameui, 752, 370, Color.CHARTREUSE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayCard(Card card, HBox cardDisplayArea) {
        if (card != null && card.sprite != null) {
            ImageView cardImageView = new ImageView(card.sprite);
            cardImageView.setFitWidth(75);
            cardImageView.setFitHeight(100);
            cardDisplayArea.getChildren().add(cardImageView);
        } else {
            System.err.println("Error: Card or sprite is null.");
        }
    }

    private void displayPlayerCard(Card card) {
        displayCard(card, playerCardDisplay);
    }

    @SuppressWarnings("unused")
    private void displayDealerCard(Card card) {
        displayCard(card, dealerCardDisplay);
    }

    private void displayInitialHands() {
        playerCardDisplay.getChildren().clear();
        dealerCardDisplay.getChildren().clear();
        for (Card card : game.getPlayerHand()) {
            displayCard(card, playerCardDisplay);
        }
        for (Card card : game.getDealerHand()) {
            displayCard(card, dealerCardDisplay);
        }

    }

    @SuppressWarnings("unused")
    private void displayPlayerHand() {
        playerCardDisplay.getChildren().clear();
        for (Card card : game.getPlayerHand()) {
            displayCard(card, playerCardDisplay);
        }
    }

    private void displayDealerHand() {
        dealerCardDisplay.getChildren().clear();
        for (Card card : game.getDealerHand()) {
            displayCard(card, dealerCardDisplay);
        }
    }

    private void updateScores() {
        playerScoreLabel.setText("Player: " + game.calculateHandValue(game.getPlayerHand()));
        dealerScoreLabel.setText("Dealer: " + game.calculateHandValue(game.getDealerHand()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}