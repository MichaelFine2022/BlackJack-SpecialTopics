package com.blackjack.ui;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.json.JSONObject;

import com.blackjack.game.Card;
import com.blackjack.game.GameLogic;
import com.blackjack.game.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent; 
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage; 
import javafx.scene.text.Font; 
import javafx.scene.text.FontWeight; 
public class GameUI { 
    private GameLogic game;
    public static Player player;
    private HBox dealerCardDisplay;
    private HBox playerCardDisplay;
    private Label playerScoreLabel;
    private Label dealerScoreLabel;
    private Button hitButton;
    private Button standButton;

    private final ObservableList<String> games = FXCollections.observableArrayList();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_URL = "http://127.0.0.1:5000/api/games";

    private Stage primaryStage; 
    private Font loadedFont; 
    public GameUI(Stage stage) {
        this.primaryStage = stage;
    }

    
    public void init() {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/Lexend.ttf")) {
            if (fontStream != null) {
                loadedFont = Font.loadFont(fontStream, 14.00); 
            } else {
                loadedFont = Font.getDefault();
            }
        } catch (IOException e) {
            loadedFont = Font.getDefault();
        }
    }

    
    public Parent createGamePane() { 
        
        player = new Player("currentGame");
        game = new GameLogic(player);
        game.startGame(); 

        GridPane gameui = new GridPane();
        gameui.setStyle("-fx-background-color: #006400;"); 
        
        javafx.scene.layout.RowConstraints row1 = new javafx.scene.layout.RowConstraints();
        javafx.scene.layout.RowConstraints row2 = new javafx.scene.layout.RowConstraints();
        row1.setVgrow(javafx.scene.layout.Priority.ALWAYS);
        gameui.getRowConstraints().addAll(row1, row2);

        String gameButtonStyle = "-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
        String gameButtonPressedStyle = "-fx-background-color: #c2c2c2;";
        
        hitButton = new Button("Hit");
        hitButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        hitButton.setPrefWidth(100.00);
        hitButton.setPrefHeight(50.00);
        hitButton.setDisable(false);
        hitButton.setStyle(gameButtonStyle);
        hitButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            hitButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null)));
        });
        hitButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            hitButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null)));
        });
        hitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Card newCard = game.playerHit();
                displayPlayerCard(newCard);
                updateScores();
                if (game.calculateHandValue(game.getPlayerHand()) > 21) {
                    hitButton.setDisable(true);
                    standButton.setDisable(true);
                    System.out.println("Player Busts!");
                    addGame(player.name, -1);
                    System.out.println("GAME RECORDED");
                    showGameEndOptions(); 
                }
            }
        });

        standButton = new Button("Stand");
        standButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        standButton.setPrefWidth(100.00);
        standButton.setPrefHeight(50.00);
        standButton.setDisable(false);
        standButton.setStyle(gameButtonStyle);
        standButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            standButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null)));
        });
        standButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            standButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null)));
        });
        standButton.setOnAction(event -> {
            hitButton.setDisable(true);
            standButton.setDisable(true);
            List<Card> dealerHand = game.dealerPlay();
            displayDealerHand();
            updateScores();
            String result = game.determineWinner();
            System.out.println(result);
            addGame(player.name, game.calculateHandValue(game.getPlayerHand()));
            System.out.println("GAME RECORDED");
            showGameEndOptions(); 
        });

        dealerCardDisplay = new HBox(5);
        playerCardDisplay = new HBox(5);
        VBox buttons = new VBox(5, hitButton, standButton);
        playerScoreLabel = new Label("Player: 0");
        playerScoreLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        playerScoreLabel.setTextFill(Color.WHITE); 
        dealerScoreLabel = new Label("Dealer: 0");
        dealerScoreLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        dealerScoreLabel.setTextFill(Color.WHITE); 

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

        return gameui; 
    }

    private void showGameEndOptions() {
        Button playAgainButton = new Button("Play Again");
        playAgainButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        playAgainButton.setPrefWidth(120);
        playAgainButton.setPrefHeight(40);
        String optionButtonStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
        String optionButtonPressedStyle = "-fx-background-color: #0056b3;";
        playAgainButton.setStyle(optionButtonStyle);
        playAgainButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { playAgainButton.setStyle(optionButtonPressedStyle); });
        playAgainButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { playAgainButton.setStyle(optionButtonStyle); });


        Button backToMenuButton = new Button("Back to Menu");
        backToMenuButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        backToMenuButton.setPrefWidth(120);
        backToMenuButton.setPrefHeight(40);
        String exitButtonStyle = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
        String exitButtonPressedStyle = "-fx-background-color: #545b62;";
        backToMenuButton.setStyle(exitButtonStyle);
        backToMenuButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { backToMenuButton.setStyle(exitButtonPressedStyle); });
        backToMenuButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { backToMenuButton.setStyle(exitButtonStyle); });

        HBox optionsBox = new HBox(15, playAgainButton, backToMenuButton);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setPadding(new Insets(20));

        GridPane currentRoot = (GridPane) primaryStage.getScene().getRoot();
        currentRoot.add(optionsBox, 0, 2); 
        GridPane.setHalignment(optionsBox, HPos.CENTER);

        hitButton.setDisable(true);
        standButton.setDisable(true);

        playAgainButton.setOnAction(event -> {
            System.out.println("Playing another game...");
            
            currentRoot.getChildren().remove(optionsBox);
            resetGame(); 
        });

        backToMenuButton.setOnAction(event -> {
            MenuPage menuPage = new MenuPage();
            try {
                menuPage.init();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error initializing MenuPage on return.");
                return;
            }
            Parent menuRoot = menuPage.createMenuPane(primaryStage); 
            primaryStage.getScene().setRoot(menuRoot);
            primaryStage.setTitle("Blackjack Menu"); 
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.centerOnScreen(); 
        });
    }

    private void resetGame() {
        game = new GameLogic(player);
        game.startGame();
        playerCardDisplay.getChildren().clear();
        dealerCardDisplay.getChildren().clear();
        displayInitialHands();
        updateScores();
        hitButton.setDisable(false);
        standButton.setDisable(false);
    }


    private void addGame(String playerInput, Integer scoreInput) {
        String player = "Admin"; 
        Integer score = scoreInput;
        try {
            JSONObject gameData = new JSONObject();
            gameData.put("player", player);
            gameData.put("score", score);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gameData.toString()))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 201) {
                            System.out.println("Game added successfully to Flask backend.");
                            Platform.runLater(() -> {
                                fetchGames(); 
                            });
                        } else {
                            System.err.println("Failed to add game: " + response.statusCode() + " - " + response.body());
                        }
                    });

        } catch (NumberFormatException e) {
            System.err.println("Invalid score format.");
        } 
    }
    private void fetchGames() {
        // IMPLEMENT THIS LATER
    }

    private void displayCard(Card card, HBox cardDisplayArea) {
        if (card != null && card.sprite != null) {
            ImageView cardImageView = new ImageView(card.sprite);
            cardImageView.setFitWidth(75);
            cardImageView.setFitHeight(100);
            cardDisplayArea.getChildren().add(cardImageView);
        } else {
            System.err.println("Error: Card or sprite is null (likely missing image for a card).");
        }
    }

    private void displayPlayerCard(Card card) {
        displayCard(card, playerCardDisplay);
    }

    private void displayDealerCard(Card card) {
        displayCard(card, dealerCardDisplay);
    }

    private void displayInitialHands() {
        playerCardDisplay.getChildren().clear();
        dealerCardDisplay.getChildren().clear();
        for (Card card : game.getPlayerHand()) {
            displayCard(card, playerCardDisplay);
        }
        displayCard(game.getDealerHand().get(0), dealerCardDisplay);
        
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


}