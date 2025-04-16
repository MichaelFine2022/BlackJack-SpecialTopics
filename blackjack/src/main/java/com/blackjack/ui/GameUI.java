package com.blackjack.ui;

import com.blackjack.game.GameLogic;
import com.blackjack.game.Player ;
import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameUI extends Application {
    private GameLogic game;
    public static Player player;
    @Override
    public void start(Stage primaryStage) {
        
        player = new Player("currentGame");
        game = new GameLogic(player);
        game.startGame();

        GridPane gameui = new GridPane();
        gameui.setPrefSize(752, 370);
        gameui.setStyle("-fx-background-color: #eeeeee;");

        Button HitButton = new Button("Hit");
        HitButton.setPrefWidth(100.00);
        HitButton.setPrefHeight(50.00);
        HitButton.setDisable(false);
        HitButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;");
        HitButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { HitButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null))); });
        HitButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { HitButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null))); });

        Button StandButton = new Button("Stand");
        StandButton.setPrefWidth(100.00);
        StandButton.setPrefHeight(50.00);
        StandButton.setDisable(false);
        StandButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { StandButton.setBackground(new Background(new BackgroundFill(Color.web("#c2c2c2"), new CornerRadii(4.00), null))); });
        StandButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { StandButton.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(4.00), null))); });
        StandButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1b1b1b; -fx-border-color: #626262; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;");
        
        HBox dealerUI = new HBox(5);
        HBox DealerCards = new HBox(5);
        dealerUI.getChildren().add(DealerCards);
        HBox playerUI = new HBox(5);
        HBox PlayerCards = new HBox(5);
        //PlayerCards.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null , null)));
        VBox buttons = new VBox(5);
        VBox PlayerScore = new VBox(5);

        buttons.getChildren().addAll(HitButton, StandButton);
        playerUI.getChildren().addAll(buttons, PlayerCards, PlayerScore);
        //playerUI.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, null , null)));
        gameui.getChildren().addAll(dealerUI, playerUI);
        gameui.setAlignment(Pos.BOTTOM_LEFT);

        GridPane.setMargin(playerUI, new Insets(10,10,10,10));
        GridPane.setMargin(dealerUI, new Insets(10,10,10,10));
        Scene scene = new Scene(gameui, 752, 370);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
