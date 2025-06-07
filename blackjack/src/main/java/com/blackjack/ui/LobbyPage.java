package com.blackjack.ui;

import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LobbyPage {

    private Stage stage;
    private VBox gameListContainer;
    private Firestore db;
    private String hostedGameId = null;
    public String currentFirebaseUserId;

    public LobbyPage(Stage stage, String currentFirebaseUserId) {
        this.stage = stage;
        this.currentFirebaseUserId = currentFirebaseUserId;
        initFirebase();
    }

    private void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                try (InputStream serviceAccount = getClass().getResourceAsStream("/firebaseServiceAccount.json")) {
                    if (serviceAccount == null) {
                        throw new IOException("firebaseServiceAccount.json not found in classpath resources. Make sure it's in src/main/resources.");
                    }
                    FirebaseOptions options = new FirebaseOptions.Builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                    System.out.println("Firebase Admin SDK initialized successfully.");
                }
            }
            db = FirestoreClient.getFirestore();
            System.out.println("Firestore client obtained.");
        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Label error = new Label("Failed to connect to Firebase. Check console for details.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
        }
    }

    public VBox createLobbyPane() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #222;");

        Label title = new Label("Multiplayer Lobby");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        gameListContainer = new VBox(10);
        gameListContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(gameListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        Button createGameButton = new Button("Create Game");
        createGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        createGameButton.setOnAction(event -> CompletableFuture.runAsync(this::createGame));

        Button stopHostingButton = new Button("Stop Hosting");
        stopHostingButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        stopHostingButton.setOnAction(event -> CompletableFuture.runAsync(this::stopHosting));

        Button reloadButton = new Button("Reload Games");
        reloadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        reloadButton.setOnAction(event -> CompletableFuture.runAsync(this::reloadGames));

        Button backButton = new Button("Back to Menu");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        backButton.setOnAction(event -> {
            MenuPage menu = new MenuPage(currentFirebaseUserId);
            try {
                menu.start(stage);
            } catch (Exception ex) {
                System.err.println("Error navigating back to menu: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        HBox bottomButtons = new HBox(20, createGameButton, stopHostingButton, reloadButton, backButton);
        bottomButtons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, scrollPane, bottomButtons);

        CompletableFuture.runAsync(this::reloadGames);

        return root;
    }

    private void reloadGames() {
        if (db == null) {
            Platform.runLater(() -> {
                Label error = new Label("Firestore not initialized.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
            return;
        }

        Platform.runLater(() -> gameListContainer.getChildren().clear());

        try {
            if (hostedGameId != null) {
                
                DocumentSnapshot snapshot = db.collection("games").document(hostedGameId).get().get();
                
                if (!snapshot.exists() || !"waiting".equals(snapshot.getString("status"))) {
                    hostedGameId = null; 
                }
            }

            
            ApiFuture<QuerySnapshot> future = db.collection("games")
                    .whereEqualTo("status", "waiting")
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            Platform.runLater(() -> {
                boolean hasVisibleGames = false;
                for (QueryDocumentSnapshot doc : documents) {
                    String gameId = doc.getId();

                
                    if (hostedGameId != null && gameId.equals(hostedGameId)) {
                        continue;
                    }

                    Map<String, Object> gameData = doc.getData();
                    String host = (String) gameData.getOrDefault("host", "Unknown");
                    String player1 = (String) gameData.get("player1");
                    String displayHost = (player1 != null && !player1.isEmpty()) ? player1 : (host != null && !host.isEmpty() ? host : "Unknown");


                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setBackground(new Background(new BackgroundFill(Color.web("#444"), CornerRadii.EMPTY, Insets.EMPTY)));
                    row.setPadding(new Insets(10));

                    Label label = new Label("Game ID: " + gameId + " | Host: " + displayHost);
                    label.setTextFill(Color.WHITE);

                    Button joinButton = new Button("Join");
                    joinButton.setOnAction(event -> CompletableFuture.runAsync(() -> joinGame(gameId)));
                    joinButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");

                    HBox.setHgrow(label, Priority.ALWAYS);

                    row.getChildren().addAll(label, joinButton);
                    gameListContainer.getChildren().add(row);

                    hasVisibleGames = true;
                }

                if (!hasVisibleGames) {
                    Label noGames = new Label("No active games found.");
                    noGames.setTextFill(Color.LIGHTGRAY);
                    gameListContainer.getChildren().add(noGames);
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to load games from Firestore: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Label error = new Label("Failed to load games from Firestore: " + e.getMessage());
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
        }
    }

    private void stopHosting() {
        if (db == null) {
            Platform.runLater(() -> {
                Label error = new Label("Firestore not initialized.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
            return;
        }

        if (hostedGameId == null) {
            System.out.println("No active game being hosted by you to stop.");
            Platform.runLater(() -> {
                Label info = new Label("You are not currently hosting any game.");
                info.setTextFill(Color.YELLOW);
                gameListContainer.getChildren().add(info);
            });
            return;
        }

        try {
            ApiFuture<WriteResult> future = db.collection("games").document(hostedGameId).delete();
            future.get();
            System.out.println("Stopped hosting game: " + hostedGameId);
            hostedGameId = null; 
            CompletableFuture.runAsync(this::reloadGames);
            Platform.runLater(() -> {
                Label info = new Label("Successfully stopped hosting your game.");
                info.setTextFill(Color.LIMEGREEN);
                gameListContainer.getChildren().add(info);
            });
        } catch (Exception e) {
            System.err.println("Failed to stop hosting game " + hostedGameId + ": " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Label error = new Label("Failed to stop hosting game: " + e.getMessage());
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
        }
    }

    private void createGame() {
        if (db == null) {
            Platform.runLater(() -> {
                Label error = new Label("Firestore not initialized. Cannot create game.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
            return;
        }
        if (currentFirebaseUserId == null || currentFirebaseUserId.isEmpty()) {
            Platform.runLater(() -> {
                Label error = new Label("User not logged in. Cannot create game.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
            return;
        }

        try {
            String newGameId = db.runTransaction(transaction -> {
                QuerySnapshot existingGameSnapshot = transaction.get(
                        db.collection("games")
                                .whereEqualTo("host", currentFirebaseUserId)
                                .whereEqualTo("status", "waiting") 
                ).get();

                if (!existingGameSnapshot.isEmpty()) {
                    DocumentSnapshot existingGame = existingGameSnapshot.getDocuments().get(0);
                    hostedGameId = existingGame.getId();
                    System.out.println("You are already hosting a game: " + hostedGameId);
                    Platform.runLater(() -> {
                        Label info = new Label("You are already hosting game: " + hostedGameId + ". Navigating to it.");
                        info.setTextFill(Color.YELLOW);
                        gameListContainer.getChildren().add(info);
                    });
                    return hostedGameId;
                }

                Map<String, Object> gameData = new HashMap<>();
                gameData.put("host", currentFirebaseUserId);
                gameData.put("player1", currentFirebaseUserId);
                gameData.put("player2", null); 
                gameData.put("status", "waiting"); 
                gameData.put("started", false); 
                gameData.put("createdAt", FieldValue.serverTimestamp());
                gameData.put("player1Cards", new ArrayList<>());
                gameData.put("player2Cards", new ArrayList<>());
                gameData.put("dealerCards", new ArrayList<>());
                gameData.put("player1Score", 0);
                gameData.put("player2Score", 0);
                gameData.put("turn", null); 
                gameData.put("gameMessage", "Waiting for another player or ready to start.");


                DocumentReference newGameRef = db.collection("games").document();
                transaction.set(newGameRef, gameData);

                hostedGameId = newGameRef.getId();
                System.out.println("New game created with ID: " + hostedGameId);
                Platform.runLater(() -> {
                    Label info = new Label("Successfully created and are hosting game: " + hostedGameId + ". Entering game.");
                    info.setTextFill(Color.LIMEGREEN);
                    gameListContainer.getChildren().add(info);
                });
                return hostedGameId;
            }).get();

            if (newGameId != null) {
                Platform.runLater(() -> navigateToGame(newGameId));
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error creating game: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Label error = new Label("Failed to create game: " + e.getMessage());
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
        }
    }

    private void joinGame(String gameId) {
        if (db == null) {
            System.err.println("Firestore not initialized. Cannot join game.");
            Platform.runLater(() -> {
                Label error = new Label("Firestore not initialized. Cannot join game.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
            return;
        }
        if (currentFirebaseUserId == null || currentFirebaseUserId.isEmpty()) {
            System.err.println("Current user ID is not set. Cannot join game.");
            Platform.runLater(() -> {
                Label error = new Label("User not logged in. Cannot join game.");
                error.setTextFill(Color.RED);
                gameListContainer.getChildren().add(error);
            });
            return;
        }
        if (hostedGameId != null && gameId.equals(hostedGameId)) {
            System.out.println("Cannot join your own hosted game.");
            Platform.runLater(() -> {
                Label info = new Label("You cannot join your own hosted game.");
                info.setTextFill(Color.YELLOW);
                gameListContainer.getChildren().add(info);
            });
            return;
        }

        DocumentReference gameRef = db.collection("games").document(gameId);

        CompletableFuture.supplyAsync(() -> {
            try {
                return db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(gameRef).get();

                    if (!snapshot.exists()) {
                        System.err.println("Game '" + gameId + "' does not exist or was deleted.");
                        Platform.runLater(() -> {
                            Label error = new Label("Game no longer exists: " + gameId);
                            error.setTextFill(Color.RED);
                            gameListContainer.getChildren().add(error);
                        });
                        return null;
                    }

                    Map<String, Object> gameData = snapshot.getData();
                    if (gameData == null) {
                        System.err.println("Game data is null for game: " + gameId);
                        Platform.runLater(() -> {
                            Label error = new Label("Game data error for: " + gameId);
                            error.setTextFill(Color.RED);
                            gameListContainer.getChildren().add(error);
                        });
                        return null;
                    }

                    String status = (String) gameData.getOrDefault("status", "unknown");
                    String player1Id = (String) gameData.get("player1");
                    String player2Id = (String) gameData.get("player2");
                    boolean started = (Boolean) gameData.getOrDefault("started", false);


                    if (currentFirebaseUserId.equals(player1Id) || (player2Id != null && currentFirebaseUserId.equals(player2Id))) {
                        System.out.println("You are already part of this game: " + gameId);
                        Platform.runLater(() -> {
                            navigateToGame(gameId); 
                        });
                        return null;
                    }

                    if (player2Id != null && !player2Id.isEmpty()) {
                        System.out.println("Game '" + gameId + "' already has two players.");
                        Platform.runLater(() -> {
                            Label info = new Label("Game '" + gameId + "' is already full.");
                            info.setTextFill(Color.YELLOW);
                            gameListContainer.getChildren().add(info);
                        });
                        return null;
                    }

                    
                    if (!"waiting".equals(status) && started && player2Id != null) {
                        System.out.println("Game '" + gameId + "' is already full or explicitly not joinable (Status: " + status + ").");
                        Platform.runLater(() -> {
                            Label info = new Label("Game '" + gameId + "' is no longer available to join.");
                            info.setTextFill(Color.YELLOW);
                            gameListContainer.getChildren().add(info);
                        });
                        return null;
                    }


                    Map<String, Object> updates = new HashMap<>();
                    updates.put("player2", currentFirebaseUserId);
                    updates.put("joinedAt", FieldValue.serverTimestamp());
                    updates.put("player2Cards", new ArrayList<>());
                    updates.put("player2Score", 0);

                    updates.put("gameMessage", "Player " + currentFirebaseUserId + " has joined. ");
                    if (started) {
                        updates.put("gameMessage", updates.get("gameMessage") + "Game is in progress. Wait for next round.");
                    } else {
                        updates.put("gameMessage", updates.get("gameMessage") + "Waiting for host to deal.");
                    }

                    transaction.update(gameRef, updates);

                    System.out.println("Successfully joined game: " + gameId + " as player2.");
                    return true;
                }).get();

            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error joining game: " + gameId + " - " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label error = new Label("Failed to join game: " + e.getMessage());
                    error.setTextFill(Color.RED);
                    gameListContainer.getChildren().add(error);
                });
                return false;
            }
        }).thenAccept(success -> {
            if (success != null && (Boolean) success) {
                Platform.runLater(() -> navigateToGame(gameId));
            }
        });
    }

    private void navigateToGame(String gameId) {
        System.out.println("Navigating to MultiplayerGame for game: " + gameId + " (Host: " + (hostedGameId != null && hostedGameId.equals(gameId) ? "Yes" : "No") + ")");
        //Displays the multiplayer Game JavaFX scene!
        MultiplayerGame game = new MultiplayerGame(stage, gameId, currentFirebaseUserId, hostedGameId != null && hostedGameId.equals(gameId));
        game.show();
    }


    public void show() {
        Scene scene = new Scene(createLobbyPane(), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Multiplayer Lobby");
        stage.show();
    }
}