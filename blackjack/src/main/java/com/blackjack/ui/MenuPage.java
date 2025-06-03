package com.blackjack.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.IOException;

public class MenuPage extends Application {
    private Font loadedFont;
    public String username;
    public MenuPage(String username) {
        this.username = username;
    }

    @Override
    public void init() throws Exception {
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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Blackjack Menu");
        BorderPane menuPane = createMenuPane(primaryStage);
        Scene scene = new Scene(menuPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public BorderPane createMenuPane(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #333333;");

        Label titleLabel = new Label("Blackjack Menu");
        titleLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 36) : Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#f0f0f0"));
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(40, 0, 0, 0));
        root.setTop(titleLabel);

        VBox buttonsContainer = new VBox(25);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(50));

        String buttonStyle = "-fx-background-color: #4CAF50;" +
                             "-fx-text-fill: white;" +
                             "-fx-border-radius: 8px;" +
                             "-fx-background-radius: 8px;" +
                             "-fx-cursor: hand;" +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);";

        String buttonPressedStyle = "-fx-background-color: #388E3C;";

        Button singlePlayerButton = new Button("Single Player");
        singlePlayerButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        singlePlayerButton.setPrefWidth(250);
        singlePlayerButton.setPrefHeight(50);
        singlePlayerButton.setStyle(buttonStyle);
        singlePlayerButton.setOnMousePressed(e -> singlePlayerButton.setStyle(buttonPressedStyle));
        singlePlayerButton.setOnMouseReleased(e -> singlePlayerButton.setStyle(buttonStyle));

        Button multiPlayerButton = new Button("Multiplayer");
        multiPlayerButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        multiPlayerButton.setPrefWidth(250);
        multiPlayerButton.setPrefHeight(50);
        multiPlayerButton.setStyle(buttonStyle);
        multiPlayerButton.setOnMousePressed(e -> multiPlayerButton.setStyle(buttonPressedStyle));
        multiPlayerButton.setOnMouseReleased(e -> multiPlayerButton.setStyle(buttonStyle));

        Button tutorialButton = new Button("Tutorial");
        tutorialButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 16) : Font.font("System", FontWeight.BOLD, 16));
        tutorialButton.setPrefWidth(250);
        tutorialButton.setPrefHeight(50);
        tutorialButton.setStyle(buttonStyle);
        tutorialButton.setOnMousePressed(e -> tutorialButton.setStyle(buttonPressedStyle));
        tutorialButton.setOnMouseReleased(e -> tutorialButton.setStyle(buttonStyle));

        buttonsContainer.getChildren().addAll(
                singlePlayerButton,
                multiPlayerButton,
                tutorialButton
        );

        root.setCenter(buttonsContainer);

        singlePlayerButton.setOnAction(event -> {
            System.out.println("Single Player button clicked! Launching GameUI...");
            
            GameUI gameUI = new GameUI(stage); 
            gameUI.init();
            Parent gameRoot = gameUI.createGamePane();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(gameRoot);
            stage.setWidth(1000); 
            stage.setHeight(700);
            stage.centerOnScreen(); 
            stage.setTitle("Blackjack Game");
        });

        multiPlayerButton.setOnAction(event -> {
            LobbyPage lobby = new LobbyPage(stage, username);
            lobby.show();
        });

        tutorialButton.setOnAction(event -> {
            System.out.println("Tutorial button clicked!");
        });

        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}