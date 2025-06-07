package com.blackjack.ui;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.blackjack.game.Card; 
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MultiplayerGame {

    private Stage stage;
    private String gameId;
    private String currentUserId;
    private boolean isHost;
    private Firestore db;
    private ListenerRegistration gameListenerRegistration;

    private Label gameMessageLabel;
    private Label player1NameLabel;
    private Label player2NameLabel;
    private HBox player1CardDisplay;
    private HBox player2CardDisplay;
    private HBox dealerCardDisplay;
    private Label player1ScoreLabel;
    private Label player2ScoreLabel;
    private Label dealerScoreLabel;
    private Button hitButton;
    private Button standButton;
    private Button dealButton; 
    private Button leaveGameButton;

    private VBox gameEndOptionsBox; 

    
    private String player1Id;
    private String player2Id;
    private String currentPlayerTurnId;
    private List<Card> player1Cards; 
    private List<Card> player2Cards; 
    private List<Card> dealerCards;  
    private int player1Score;
    private int player2Score;
    private int dealerScore;
    private String currentStatus; 
    private boolean started; 
    private Font loadedFont; 
    private Image CARD_BACK_IMAGE;
    
        public MultiplayerGame(Stage stage, String gameId, String currentUserId, boolean isHost) {
            this.stage = stage;
            this.gameId = gameId;
            this.currentUserId = currentUserId;
            this.isHost = isHost;
            this.db = FirestoreClient.getFirestore();
            loadCustomFont();
    
            
            try (InputStream inputStream = getClass().getResourceAsStream("/cards/cardBack.png")) {
                if (inputStream != null) {
                    CARD_BACK_IMAGE = new Image(inputStream);
                } else {
                    System.err.println("Could not load card back image: /cards/back.png");
                    CARD_BACK_IMAGE = null;
                }
            } catch (IOException e) {
                System.err.println("Error loading card back image: " + e.getMessage());
                CARD_BACK_IMAGE = null;
        }
    }

    private void loadCustomFont() {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/Lexend.ttf")) {
            if (fontStream != null) {
                loadedFont = Font.loadFont(fontStream, 14.00);
            } else {
                System.err.println("Lexend.ttf not found. Using default font.");
                loadedFont = Font.getDefault();
            }
        } catch (IOException e) {
            System.err.println("Error loading font: " + e.getMessage());
            loadedFont = Font.getDefault();
        }
    }

    public void show() {
        GridPane gameGrid = new GridPane();
        gameGrid.setStyle("-fx-background-color: #006400;"); 
        gameGrid.setPadding(new Insets(20));
        gameGrid.setVgap(15);
        gameGrid.setHgap(15);
        gameGrid.setAlignment(Pos.CENTER); 

        javafx.scene.layout.RowConstraints row1 = new javafx.scene.layout.RowConstraints(); 
        javafx.scene.layout.RowConstraints row2 = new javafx.scene.layout.RowConstraints(); 
        row1.setVgrow(javafx.scene.layout.Priority.ALWAYS);
        row2.setVgrow(javafx.scene.layout.Priority.ALWAYS);
        gameGrid.getRowConstraints().addAll(row1, row2);

        dealerCardDisplay = new HBox(5);
        dealerCardDisplay.setAlignment(Pos.CENTER);
        dealerScoreLabel = new Label("Dealer: 0");
        dealerScoreLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        dealerScoreLabel.setTextFill(Color.WHITE);

        VBox dealerInfo = new VBox(5);
        dealerInfo.setAlignment(Pos.CENTER);
        Label dealerTitle = new Label("Dealer's Hand:");
        dealerTitle.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 18) : Font.font("System", FontWeight.BOLD, 18));
        dealerTitle.setTextFill(Color.LIGHTGRAY);
        dealerInfo.getChildren().addAll(dealerTitle, dealerScoreLabel);


        HBox dealerUI = new HBox(10, dealerInfo, dealerCardDisplay);
        dealerUI.setAlignment(Pos.CENTER);
        GridPane.setHalignment(dealerUI, HPos.CENTER);
        GridPane.setMargin(dealerUI, new Insets(10));
        gameGrid.add(dealerUI, 0, 0);

        HBox playersHandsContainer = new HBox(50); 
        playersHandsContainer.setAlignment(Pos.CENTER);
        GridPane.setHalignment(playersHandsContainer, HPos.CENTER);
        GridPane.setMargin(playersHandsContainer, new Insets(10));
        gameGrid.add(playersHandsContainer, 0, 1);

        player1CardDisplay = new HBox(5);
        player1CardDisplay.setAlignment(Pos.CENTER);
        player1ScoreLabel = new Label("Player: 0");
        player1ScoreLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        player1ScoreLabel.setTextFill(Color.WHITE);

        player2CardDisplay = new HBox(5);
        player2CardDisplay.setAlignment(Pos.CENTER);
        player2ScoreLabel = new Label("Score: 0");
        player2ScoreLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        player2ScoreLabel.setTextFill(Color.WHITE);

        String gameButtonStyle = "-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
        String gameButtonPressedStyle = "-fx-background-color: #c2c2c2;";

        hitButton = new Button("Hit");
        hitButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        hitButton.setPrefWidth(100.00);
        hitButton.setPrefHeight(50.00);
        hitButton.setStyle(gameButtonStyle);
        addHoverEffect(hitButton, gameButtonStyle, gameButtonPressedStyle);
        hitButton.setOnAction(e -> CompletableFuture.runAsync(() -> handleHit()));
        hitButton.setDisable(true);

        standButton = new Button("Stand");
        standButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        standButton.setPrefWidth(100.00);
        standButton.setPrefHeight(50.00);
        standButton.setStyle(gameButtonStyle);
        addHoverEffect(standButton, gameButtonStyle, gameButtonPressedStyle);
        standButton.setOnAction(e -> CompletableFuture.runAsync(() -> handleStand()));
        standButton.setDisable(true);

        VBox actionButtons = new VBox(5, hitButton, standButton);
        actionButtons.setAlignment(Pos.CENTER);

        VBox player1Box = new VBox(5);
        player1Box.setAlignment(Pos.CENTER);
        player1NameLabel = new Label("Player 1 (Host):");
        player1NameLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 18) : Font.font("System", FontWeight.BOLD, 18));
        player1NameLabel.setTextFill(Color.WHITE);
        HBox player1Content = new HBox(10, actionButtons, player1CardDisplay); 
        player1Content.setAlignment(Pos.CENTER);
        player1Box.getChildren().addAll(player1NameLabel, player1Content, player1ScoreLabel);
        playersHandsContainer.getChildren().add(player1Box);

        VBox player2Box = new VBox(5);
        player2Box.setAlignment(Pos.CENTER);
        player2NameLabel = new Label("Player 2:");
        player2NameLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 18) : Font.font("System", FontWeight.BOLD, 18));
        player2NameLabel.setTextFill(Color.WHITE);
        player2Box.getChildren().addAll(player2NameLabel, player2CardDisplay, player2ScoreLabel);
        playersHandsContainer.getChildren().add(player2Box);


        
        VBox bottomControlsArea = new VBox(10);
        bottomControlsArea.setAlignment(Pos.CENTER);
        bottomControlsArea.setPadding(new Insets(10, 20, 20, 20)); 

        gameMessageLabel = new Label("Welcome to Multiplayer Blackjack!");
        gameMessageLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 20) : Font.font("System", FontWeight.BOLD, 20));
        gameMessageLabel.setTextFill(Color.WHITE);

        HBox bottomButtons = new HBox(20);
        bottomButtons.setAlignment(Pos.CENTER);

        dealButton = new Button("Deal New Round");
        dealButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        dealButton.setPrefWidth(150.00);
        dealButton.setPrefHeight(50.00);
        dealButton.setStyle(gameButtonStyle);
        addHoverEffect(dealButton, gameButtonStyle, gameButtonPressedStyle);
        dealButton.setOnAction(e -> CompletableFuture.runAsync(() -> handleDealNewRound()));
        dealButton.setVisible(isHost);
        dealButton.setManaged(isHost);
        dealButton.setDisable(true);

        leaveGameButton = new Button("Leave Game");
        leaveGameButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
        leaveGameButton.setPrefWidth(120);
        leaveGameButton.setPrefHeight(50);
        String exitButtonStyle = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
        String exitButtonPressedStyle = "-fx-background-color: #d32f2f;";
        leaveGameButton.setStyle(exitButtonStyle);
        addHoverEffect(leaveGameButton, exitButtonStyle, exitButtonPressedStyle);
        leaveGameButton.setOnAction(e -> leaveGame());

        bottomButtons.getChildren().addAll(dealButton, leaveGameButton);

        gameEndOptionsBox = new VBox(15);
        gameEndOptionsBox.setAlignment(Pos.CENTER);
        gameEndOptionsBox.setPadding(new Insets(20));
        gameEndOptionsBox.setVisible(false);
        gameEndOptionsBox.setManaged(false);

        bottomControlsArea.getChildren().addAll(gameMessageLabel, bottomButtons, gameEndOptionsBox);

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #006400;"); 
        root.getChildren().addAll(gameGrid, bottomControlsArea);


        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Multiplayer Blackjack Game: " + gameId);
        stage.show();

        listenToGameDocument();
    }

    private void addHoverEffect(Button button, String defaultStyle, String pressedStyle) {
        button.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            String colorHex = pressedStyle.split("-fx-background-color: ")[1].split(";")[0].trim();
            button.setBackground(new Background(new BackgroundFill(Color.web(colorHex), new CornerRadii(4.00), null)));
        });
        button.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            String colorHex = defaultStyle.split("-fx-background-color: ")[1].split(";")[0].trim();
            button.setBackground(new Background(new BackgroundFill(Color.web(colorHex), new CornerRadii(4.00), null)));
        });
    }

    private void listenToGameDocument() {
        if (db == null) {
            System.err.println("Firestore not initialized for MultiplayerGame.");
            return;
        }

        DocumentReference gameRef = db.collection("games").document(gameId);

        gameListenerRegistration = gameRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.err.println("Listen failed for game " + gameId + ": " + e);
                Platform.runLater(() -> {
                    gameMessageLabel.setText("Error: Could not load game data.");
                    gameMessageLabel.setTextFill(Color.RED);
                });
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> gameData = snapshot.getData();
                if (gameData == null) return;

                updateUIFromGameData(gameData);
            } else {
                Platform.runLater(() -> {
                    gameMessageLabel.setText("Game has ended or been deleted.");
                    gameMessageLabel.setTextFill(Color.RED);
                    showGameEndOptions(true);
                });
            }
        });
    }

    private void updateUIFromGameData(Map<String, Object> gameData) {
        Platform.runLater(() -> {
            player1Id = (String) gameData.getOrDefault("player1", "N/A");
            player2Id = (String) gameData.getOrDefault("player2", null);
            currentPlayerTurnId = (String) gameData.getOrDefault("turn", null);
            currentStatus = (String) gameData.getOrDefault("status", "unknown");
            started = (Boolean) gameData.getOrDefault("started", false);

            player1Cards = toCardList((List<String>) gameData.getOrDefault("player1Cards", Collections.emptyList()));
            player2Cards = toCardList((List<String>) gameData.getOrDefault("player2Cards", Collections.emptyList()));
            dealerCards = toCardList((List<String>) gameData.getOrDefault("dealerCards", Collections.emptyList()));

            player1Score = ((Long) gameData.getOrDefault("player1Score", 0L)).intValue();
            player2Score = ((Long) gameData.getOrDefault("player2Score", 0L)).intValue();
            dealerScore = ((Long) gameData.getOrDefault("dealerScore", 0L)).intValue();

            String gameMessage = (String) gameData.getOrDefault("gameMessage", "");
            if (!gameMessage.isEmpty()) {
                gameMessageLabel.setText(gameMessage);
                gameMessageLabel.setTextFill(Color.WHITE);
            }

            player1NameLabel.setText("Player 1 (Host): " + player1Id);
            player2NameLabel.setText("Player 2: " + (player2Id != null && !player2Id.isEmpty() ? player2Id : "Waiting for player..."));

            displayCards(player1Cards, player1CardDisplay, false);

            boolean revealDealerCards = "finished".equals(currentStatus) || "dealer".equals(currentPlayerTurnId);
            if (!revealDealerCards && dealerCards.size() >= 2) {

                List<Card> partialDealerHand = new ArrayList<>();
                partialDealerHand.add(dealerCards.get(0));
               
                partialDealerHand.add(null); 
                displayCards(partialDealerHand, dealerCardDisplay, true); 
            } else {
                displayCards(dealerCards, dealerCardDisplay, false);
            }

            player2CardDisplay.getChildren().clear();
            if (player2Id != null && !player2Id.isEmpty()) {
                displayCards(player2Cards, player2CardDisplay, false);
            }

            player1ScoreLabel.setText("Player: " + player1Score);
            player2ScoreLabel.setText("Player 2: " + player2Score);
            dealerScoreLabel.setText("Dealer: " + (revealDealerCards ? dealerScore : "?"));

            boolean isMyTurn = currentUserId.equals(currentPlayerTurnId);
            boolean gameInProgress = "in-progress".equals(currentStatus);
            boolean gameEnded = "finished".equals(currentStatus);

            hitButton.setDisable(!isMyTurn || !gameInProgress || gameEnded);
            standButton.setDisable(!isMyTurn || !gameInProgress || gameEnded);

            if (isHost) {
                dealButton.setVisible(true);
                dealButton.setManaged(true);
                dealButton.setDisable(gameInProgress && !gameEnded);
                if (("waiting".equals(currentStatus) && !started) || (gameEnded && player2Id != null && !player2Id.isEmpty())) {
                    dealButton.setDisable(false);
                }
            } else {
                dealButton.setVisible(false);
                dealButton.setManaged(false);
            }

            showGameEndOptions(gameEnded);

            if ("waiting".equals(currentStatus) && !started) {
                gameMessageLabel.setText(isHost ? "Ready to deal, or waiting for Player 2 to join." : "Waiting for host to deal.");
                gameMessageLabel.setTextFill(Color.YELLOW);
            } else if (gameInProgress) {
                if (currentPlayerTurnId != null) {
                    String turnMessage = (currentUserId.equals(currentPlayerTurnId)) ? "It's your turn!" : "Waiting for " + (currentUserId.equals(player1Id) ? player2Id : player1Id) + "'s turn...";
                    gameMessageLabel.setText(turnMessage);
                    gameMessageLabel.setTextFill(Color.CYAN);
                }
            } else if (gameEnded) {
                gameMessageLabel.setTextFill(Color.ORANGE);
            }
        });
    }

    private void displayCards(List<Card> cards, HBox cardDisplayArea, boolean hideSecondCard) {
        cardDisplayArea.getChildren().clear();
        if (cards == null || cards.isEmpty()) {
            return;
        }

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            Image cardImage = null;

            if (hideSecondCard && i == 1) {
                cardImage = CARD_BACK_IMAGE; 
            } else if (card != null && card.sprite != null) {
                cardImage = card.sprite;
            }

            if (cardImage != null) {
                ImageView cardImageView = new ImageView(cardImage);
                cardImageView.setFitWidth(75);
                cardImageView.setFitHeight(100);
                cardDisplayArea.getChildren().add(cardImageView);
            } else {
                System.err.println("Error: Card or sprite is null (likely missing image for a card, or placeholder needed). Card: " + (card != null ? card.toString() : "null"));
                Label fallbackLabel = new Label(card != null ? card.getRank() + card.getSuit() : "ERR");
                fallbackLabel.setTextFill(Color.RED);
                fallbackLabel.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                cardDisplayArea.getChildren().add(fallbackLabel);
            }
        }
    }

    private void handleHit() {
        DocumentReference gameRef = db.collection("games").document(gameId);

        CompletableFuture.runAsync(() -> {
            try {
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(gameRef).get();
                    Map<String, Object> gameData = snapshot.getData();

                    String currentTurnInDb = (String) gameData.getOrDefault("turn", null);
                    String statusInDb = (String) gameData.getOrDefault("status", "unknown");

                    if (!currentUserId.equals(currentTurnInDb) || !"in-progress".equals(statusInDb)) {
                        throw new IllegalStateException("Cannot hit. Not your turn or game not in progress.");
                    }

                    List<String> playerCurrentCardsStrings;
                    String playerCardsField;
                    String playerScoreField;

                    if (currentUserId.equals(player1Id)) {
                        playerCurrentCardsStrings = (List<String>) gameData.getOrDefault("player1Cards", new ArrayList<>());
                        playerCardsField = "player1Cards";
                        playerScoreField = "player1Score";
                    } else if (currentUserId.equals(player2Id)) {
                        playerCurrentCardsStrings = (List<String>) gameData.getOrDefault("player2Cards", new ArrayList<>());
                        playerCardsField = "player2Cards";
                        playerScoreField = "player2Score";
                    } else {
                        throw new IllegalStateException("You are not a recognized player in this game.");
                    }

                    List<Card> playerCurrentCards = toCardList(playerCurrentCardsStrings);

                    Card newCard = getRandomCard();
                    playerCurrentCards.add(newCard);
                    int playerNewScore = calculateHandValue(playerCurrentCards);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put(playerCardsField, toStringList(playerCurrentCards));
                    updates.put(playerScoreField, playerNewScore);

                    String message = (currentUserId.equals(player1Id) ? "Player 1" : "Player 2") + " hits. Current score: " + playerNewScore;
                    updates.put("gameMessage", message);

                    if (playerNewScore > 21) {
                        message = (currentUserId.equals(player1Id) ? "Player 1" : "Player 2") + " BUSTS! Score: " + playerNewScore;
                        updates.put("gameMessage", message);
                        updates.put("status", "finished");
                        updates.put("turn", null);
                    }

                    transaction.update(gameRef, updates);
                    return null;
                }).get();
            } catch (Exception e) {
                Throwable causeToDisplay = e;
                if (e instanceof ExecutionException && e.getCause() != null) {
                    causeToDisplay = e.getCause();
                }
                final Throwable finalCause = causeToDisplay;
                System.err.println("Error hitting: " + finalCause.getMessage());
                Platform.runLater(() -> {
                    gameMessageLabel.setText(finalCause.getMessage());
                    gameMessageLabel.setTextFill(Color.RED);
                });
            }
        });
    }

    private void handleStand() {
        DocumentReference gameRef = db.collection("games").document(gameId);

        CompletableFuture.runAsync(() -> {
            try {
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(gameRef).get();
                    Map<String, Object> gameData = snapshot.getData();

                    String currentTurnInDb = (String) gameData.getOrDefault("turn", null);
                    String statusInDb = (String) gameData.getOrDefault("status", "unknown");
                    String p1Id = (String) gameData.getOrDefault("player1", null);
                    String p2Id = (String) gameData.getOrDefault("player2", null);

                    if (!currentUserId.equals(currentTurnInDb) || !"in-progress".equals(statusInDb)) {
                        throw new IllegalStateException("Cannot stand. Not your turn or game not in progress.");
                    }

                    String nextTurnId = null;
                    if (currentUserId.equals(p1Id)) {
                        if (p2Id != null && !p2Id.isEmpty() && !((Boolean)gameData.getOrDefault("player2Stood", false)) && calculateHandValue(toCardList((List<String>)gameData.getOrDefault("player2Cards", Collections.emptyList()))) <= 21) {
                            nextTurnId = p2Id;
                        } else {
                            nextTurnId = "dealer";
                        }
                    } else if (currentUserId.equals(p2Id)) {
                        nextTurnId = "dealer";
                    } else {
                        throw new IllegalStateException("You are not a recognized player in this game.");
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("turn", nextTurnId);
                    updates.put("gameMessage", (currentUserId.equals(p1Id) ? "Player 1" : "Player 2") + " stands. Turn to " + (nextTurnId != null ? nextTurnId : "end of round") + ".");

                    if (currentUserId.equals(p1Id)) {
                         updates.put("player1Stood", true); 
                    } else if (currentUserId.equals(p2Id)) {
                         updates.put("player2Stood", true); 
                    }

                    if ("dealer".equals(nextTurnId)) {
                        List<Card> dealerHand = toCardList((List<String>) gameData.getOrDefault("dealerCards", new ArrayList<>()));
                        int dealerScore = calculateHandValue(dealerHand);

                        while (dealerScore < 17) {
                            Card newCard = getRandomCard();
                            dealerHand.add(newCard);
                            dealerScore = calculateHandValue(dealerHand);
                        }
                        updates.put("dealerCards", toStringList(dealerHand));  
                        updates.put("dealerScore", dealerScore);

                        int player1ScoreFinal = ((Long) gameData.getOrDefault("player1Score", 0L)).intValue();
                        int player2ScoreFinal = (p2Id != null && !p2Id.isEmpty()) ? ((Long) gameData.getOrDefault("player2Score", 0L)).intValue() : 0;

                        String resultMessage = determineWinner(player1ScoreFinal, player2ScoreFinal, dealerScore, p1Id, p2Id);
                        updates.put("gameMessage", "Dealer plays. " + resultMessage);
                        updates.put("status", "finished");
                        updates.put("turn", null);
                    }

                    transaction.update(gameRef, updates);
                    return null;
                }).get();
            } catch (Exception e) {
                Throwable causeToDisplay = e;
                if (e instanceof ExecutionException && e.getCause() != null) {
                    causeToDisplay = e.getCause();
                }
                final Throwable finalCause = causeToDisplay;
                System.err.println("Error standing: " + finalCause.getMessage());
                Platform.runLater(() -> {
                    gameMessageLabel.setText(finalCause.getMessage());
                    gameMessageLabel.setTextFill(Color.RED);
                });
            }
        });
    }

    private void handleDealNewRound() {
        if (!isHost) {
            Platform.runLater(() -> {
                gameMessageLabel.setText("Only the host can deal a new round.");
                gameMessageLabel.setTextFill(Color.RED);
            });
            return;
        }

        DocumentReference gameRef = db.collection("games").document(gameId);

        CompletableFuture.runAsync(() -> {
            try {
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(gameRef).get();
                    Map<String, Object> gameData = snapshot.getData();

                    String p1Id = (String) gameData.getOrDefault("player1", null);
                    String p2Id = (String) gameData.getOrDefault("player2", null);

                    if (p1Id == null) {
                        throw new IllegalStateException("Host (player1) not found in game data.");
                    }

                    List<Card> newPlayer1Cards = new ArrayList<>(Arrays.asList(getRandomCard(), getRandomCard()));
                    List<Card> newPlayer2Cards = new ArrayList<>();
                    List<Card> newDealerCards = new ArrayList<>(Arrays.asList(getRandomCard(), getRandomCard()));

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("player1Cards", toStringList(newPlayer1Cards)); 
                    updates.put("player1Score", calculateHandValue(newPlayer1Cards));

                    if (p2Id != null && !p2Id.isEmpty()) {
                        newPlayer2Cards.addAll(Arrays.asList(getRandomCard(), getRandomCard()));
                        updates.put("player2Cards", toStringList(newPlayer2Cards)); 
                        updates.put("player2Score", calculateHandValue(newPlayer2Cards));
                    } else {
                        updates.put("player2Cards", new ArrayList<>()); 
                        updates.put("player2Score", 0);
                    }

                    updates.put("dealerCards", toStringList(newDealerCards)); 
                    updates.put("dealerScore", calculateHandValue(newDealerCards));

                    updates.put("turn", p1Id);
                    updates.put("status", "in-progress");
                    updates.put("started", true);
                    updates.put("gameMessage", "New round started! " + p1Id + "'s turn.");
                    updates.put("lastDealtAt", FieldValue.serverTimestamp());
                    updates.put("player1Stood", false); 
                    updates.put("player2Stood", false); 


                    transaction.update(gameRef, updates);
                    System.out.println("New round dealt for game: " + gameId + " by host " + currentUserId);
                    return null;
                }).get();
            } catch (Exception e) {
                Throwable causeToDisplay = e;
                if (e instanceof ExecutionException && e.getCause() != null) {
                    causeToDisplay = e.getCause();
                }
                final Throwable finalCause = causeToDisplay;
                System.err.println("Error dealing new round: " + finalCause.getMessage());
                Platform.runLater(() -> {
                    gameMessageLabel.setText("Failed to deal new round: " + finalCause.getMessage());
                    gameMessageLabel.setTextFill(Color.RED);
                });
            }
        });
    }

    private void leaveGame() {
        if (gameListenerRegistration != null) {
            gameListenerRegistration.remove();
            System.out.println("Game listener removed for game " + gameId);
        }

        CompletableFuture.runAsync(() -> {
            try {
                DocumentReference gameRef = db.collection("games").document(gameId);
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(gameRef).get();
                    if (snapshot.exists()) {
                        String currentStatusInDb = (String) snapshot.get("status");

                        if (isHost) {
                            transaction.delete(gameRef);
                            System.out.println("Host (" + currentUserId + ") deleted game " + gameId);
                        } else {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("player2", null);
                            updates.put("player2Cards", FieldValue.delete());
                            updates.put("player2Score", FieldValue.delete());
                            updates.put("player2Stood", FieldValue.delete());
                            if ("in-progress".equals(currentStatusInDb) || "finished".equals(currentStatusInDb) && player1Id != null) {
                                updates.put("status", "waiting");
                                updates.put("turn", null);
                                updates.put("gameMessage", "Player 2 has left. Waiting for a new player.");
                                updates.put("started", false);
                            }
                            transaction.update(gameRef, updates);
                            System.out.println("Player (" + currentUserId + ") left game " + gameId + ". Player 2 slot cleared.");
                        }
                    }
                    return null;
                }).get();
            } catch (Exception e) {
                System.err.println("Error processing game leave for " + currentUserId + " in game " + gameId + ": " + e.getMessage());
            } finally {
                Platform.runLater(() -> {
                    LobbyPage lobby = new LobbyPage(stage, currentUserId);
                    lobby.show();
                });
            }
        });
    }

    private Card getRandomCard() {
        List<String> suits = Arrays.asList("S", "H", "D", "C");
        List<String> ranks = Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
        String suit = suits.get((int)(Math.random() * suits.size()));
        String rank = ranks.get((int)(Math.random() * ranks.size()));
        return new Card(suit, rank);
    }

    private List<Card> toCardList(List<String> cardStrings) {
        if (cardStrings == null) {
            return new ArrayList<>();
        }
        return cardStrings.stream()
                .map(s -> {
                    //There should never be a card name smaller than 2
                    if (s.length() < 2) {
                        System.err.println("Invalid card string format: " + s);
                        return null; 
                    }
                    String suitStr = s.substring(0, 1);
                    String rankStr = s.substring(1);
                    return new Card(suitStr, rankStr);
                })
                .filter(java.util.Objects::nonNull) 
                .collect(Collectors.toList());
    }

    // Converts a list of cards to a list of Strings for the Firebase database
    private List<String> toStringList(List<Card> cards) {
        if (cards == null) {
            return new ArrayList<>();
        }
        return cards.stream()
                .map(card -> card.getSuit() + card.getRank())
                .collect(Collectors.toList());
    }

    private int calculateHandValue(List<Card> hand) {
        if (hand == null || hand.isEmpty()) {
            return 0;
        }
        int value = 0;
        int numAces = 0;
        for (Card card : hand) {
            if (card == null) continue; 
            if (card.getRank().equals("A")) {
                numAces++;
                value += 11;
            } else {
                value += card.getValue();
            }
        }

        while (value > 21 && numAces > 0) {
            value -= 10;
            numAces--;
        }
        return value;
    }

    private String determineWinner(int player1Score, int player2Score, int dealerScore, String p1Id, String p2Id) {
        StringBuilder result = new StringBuilder();
        result.append("Results: ");

        if (player1Score > 21) {
            result.append(p1Id).append(" busts! ");
        } else if (dealerScore > 21 || player1Score > dealerScore) {
            result.append(p1Id).append(" wins! ");
        } else if (player1Score < dealerScore) {
            result.append(p1Id).append(" loses! ");
        } else {
            result.append(p1Id).append(" pushes! ");
        }

        if (p2Id != null && !p2Id.isEmpty() && player2Score > 0) {
            result.append(p2Id).append(": ");
            if (player2Score > 21) {
                result.append("busts! ");
            } else if (dealerScore > 21 || player2Score > dealerScore) {
                result.append("wins! ");
            } else if (player2Score < dealerScore) {
                result.append("loses! ");
            } else {
                result.append("pushes! ");
            }
        }
        return result.toString().trim();
    }

    private void showGameEndOptions(boolean show) {
        if (show) {
            if (gameEndOptionsBox.getChildren().isEmpty()) {
                Button playAgainButton = new Button("Play Again");
                playAgainButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
                playAgainButton.setPrefWidth(120);
                playAgainButton.setPrefHeight(40);
                String optionButtonStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
                String optionButtonPressedStyle = "-fx-background-color: #0056b3;";
                playAgainButton.setStyle(optionButtonStyle);
                addHoverEffect(playAgainButton, optionButtonStyle, optionButtonPressedStyle);
                playAgainButton.setOnAction(event -> {
                    System.out.println("Playing another game (multiplayer reset)...");
                    if (isHost) {
                        handleDealNewRound();
                    } else {
                        Platform.runLater(() -> {
                            gameMessageLabel.setText("Waiting for host to deal a new round.");
                            gameMessageLabel.setTextFill(Color.YELLOW);
                        });
                    }
                    showGameEndOptions(false);
                });

                Button backToLobbyButton = new Button("Back to Lobby");
                backToLobbyButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 14) : Font.font("System", FontWeight.BOLD, 14));
                backToLobbyButton.setPrefWidth(120);
                backToLobbyButton.setPrefHeight(40);
                String exitButtonStyle = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
                String exitButtonPressedStyle = "-fx-background-color: #545b62;";
                backToLobbyButton.setStyle(exitButtonStyle);
                addHoverEffect(backToLobbyButton, exitButtonStyle, exitButtonPressedStyle);
                backToLobbyButton.setOnAction(event -> {
                    leaveGame();
                });

                gameEndOptionsBox.getChildren().addAll(playAgainButton, backToLobbyButton);
            }
            gameEndOptionsBox.setVisible(true);
            gameEndOptionsBox.setManaged(true);
        } else {
            gameEndOptionsBox.setVisible(false);
            gameEndOptionsBox.setManaged(false);
        }
    }
}