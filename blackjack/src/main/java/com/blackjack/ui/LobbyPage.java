package com.blackjack.ui;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.auth.oauth2.GoogleCredentials;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

public class LobbyPage {

    private Stage stage;
    private VBox gameListContainer;
    private Firestore db;
    private String hostedGameId = null; 
    public String username;
    public LobbyPage(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
        initFirebase();
    }

    private void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream("blackjack/src/main/resources/firebaseServiceAccount.json");
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
            db = FirestoreClient.getFirestore();
        } catch (Exception e) {
            e.printStackTrace();
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

        Button createGameButton = new Button("Create Game");
        createGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        createGameButton.setOnAction(e -> createGame());

        Button stopHostingButton = new Button("Stop Hosting");
        stopHostingButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        stopHostingButton.setOnAction(e -> stopHosting());

        Button reloadButton = new Button("Reload Games");
        reloadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        reloadButton.setOnAction(e -> reloadGames());

        Button backButton = new Button("Back to Menu");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        backButton.setOnAction(e -> {
            MenuPage menu = new MenuPage(username);
            try {
                menu.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox bottomButtons = new HBox(20, createGameButton, stopHostingButton, reloadButton, backButton);
        bottomButtons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, scrollPane, bottomButtons);

        reloadGames(); 

        return root;
    }

    private void reloadGames() {
        gameListContainer.getChildren().clear();

        if (db == null) {
            Label error = new Label("Firestore not initialized.");
            error.setTextFill(Color.RED);
            gameListContainer.getChildren().add(error);
            return;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection("games")
                    .whereEqualTo("status", "waiting")
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                Label noGames = new Label("No active games found.");
                noGames.setTextFill(Color.LIGHTGRAY);
                gameListContainer.getChildren().add(noGames);
            } else {
                for (QueryDocumentSnapshot doc : documents) {
                    Map<String, Object> gameData = doc.getData();
                    String host = (String) gameData.getOrDefault("host", "Unknown");
                    String gameId = doc.getId();

                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setBackground(new Background(new BackgroundFill(Color.web("#444"), CornerRadii.EMPTY, Insets.EMPTY)));
                    row.setPadding(new Insets(10));

                    Label label = new Label("Game ID: " + gameId + " | Host: " + host);
                    label.setTextFill(Color.WHITE);

                    Button joinButton = new Button("Join");
                    joinButton.setOnAction(e -> joinGame(gameId));

                    row.getChildren().addAll(label, joinButton);
                    gameListContainer.getChildren().add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Label error = new Label("Failed to load games from Firestore.");
            error.setTextFill(Color.RED);
            gameListContainer.getChildren().add(error);
        }
    }

    private void createGame() {
        if (db == null) {
            System.err.println("Firestore not initialized.");
            return;
        }

        try {
            Map<String, Object> gameData = Map.of(
                    "host", "" + username + System.currentTimeMillis(), 
                    "status", "waiting",
                    "createdAt", FieldValue.serverTimestamp()
            );

            ApiFuture<DocumentReference> future = db.collection("games").add(gameData);
            DocumentReference ref = future.get(); 
            hostedGameId = ref.getId(); 
            System.out.println("New game created: " + hostedGameId);

            reloadGames(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopHosting() {
        if (hostedGameId == null) {
            System.out.println("No active game being hosted.");
            return;
        }

        try {
            ApiFuture<WriteResult> future = db.collection("games").document(hostedGameId).delete();
            future.get(); 
            System.out.println("Stopped hosting game: " + hostedGameId);
            hostedGameId = null;
            reloadGames(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinGame(String gameId) {
        System.out.println("Joining game: " + gameId);
        // TODO: Launch multiplayer game scene with gameId
    }

    public void show() {
        Scene scene = new Scene(createLobbyPane(), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Multiplayer Lobby");
        stage.show();
    }
}
