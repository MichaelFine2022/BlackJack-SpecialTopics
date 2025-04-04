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

        Pane pane = new Pane();
        pane.setPrefSize(752, 370);
        pane.setStyle("-fx-background-color: #eeeeee;");

        Button HitButton = new Button("Hit");
        HitButton.setLayoutX(180.00);
        HitButton.setLayoutY(285.00);
        HitButton.setPrefWidth(100.00);
        HitButton.setPrefHeight(50.00);
        HitButton.setDisable(false);
        HitButton.setFont(Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Lexend.ttf"), 14.00));
        HitButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;");
        HitButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { HitButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null))); });
        HitButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { HitButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null))); });
        pane.getChildren().add(HitButton);

        Button StandButton = new Button("Stand");
        StandButton.setLayoutX(400.00);
        StandButton.setLayoutY(285.00);
        StandButton.setPrefWidth(100.00);
        StandButton.setPrefHeight(50.00);
        StandButton.setDisable(false);
        StandButton.setFont(Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Lexend.ttf"), 14.00));
        StandButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;");
        StandButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { StandButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null))); });
        StandButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { StandButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null))); });
        pane.getChildren().add(StandButton);

        Scene scene = new Scene(pane, 752, 370);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
