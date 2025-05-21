package com.blackjack.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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

public class Login extends Application {

    private Font loadedFont;

    @Override
    public void init() throws Exception {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/Lexend.ttf")) {
            if (fontStream != null) {
                loadedFont = Font.loadFont(fontStream, 14.00);
                System.out.println("Lexend loaded");
            } else {
                System.err.println("ERROR: Lexend never loaded");
                loadedFont = Font.getDefault(); 
            }
        } catch (IOException e) {
            System.err.println("ERROR: Error with font: " + e.getMessage());
            loadedFont = Font.getDefault(); 
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Blackjack Login");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;"); 

        VBox loginForm = new VBox(15); 
        loginForm.setAlignment(Pos.CENTER); 
        loginForm.setPadding(new Insets(30)); 
        loginForm.setStyle(
            "-fx-background-color: #ffffff;" + 
            "-fx-border-color: #d3d3d3;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);" 
        );
        loginForm.setMaxWidth(350); 

        Label titleLabel = new Label("Welcome to Blackjack!");
        titleLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 24) : Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(loadedFont); 
        usernameLabel.setStyle("-fx-text-fill: #555555;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username"); 
        usernameField.setFont(loadedFont); 
        usernameField.setPrefHeight(32); 
        usernameField.setMaxWidth(250); 
        usernameField.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-text-fill: #1b1b1b;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-prompt-text-fill: #737674;" +
            "-fx-padding: 5px 10px;" 
        );

        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(loadedFont);
        passwordLabel.setStyle("-fx-text-fill: #555555;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password"); 
        passwordField.setFont(loadedFont);
        passwordField.setPrefHeight(32); 
        passwordField.setMaxWidth(250);
        passwordField.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-text-fill: #1b1b1b;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-prompt-text-fill: #737674;" +
            "-fx-padding: 5px 10px;"
        );

        Label errorMessageLabel = new Label(""); 
        errorMessageLabel.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 13) : Font.font("System", FontWeight.BOLD, 13));
        errorMessageLabel.setTextFill(Color.web("#dc3545")); 
        errorMessageLabel.setVisible(false); 

        Button loginButton = new Button("Login");
        loginButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 15) : Font.font("System", FontWeight.BOLD, 15));
        loginButton.setPrefWidth(200); 
        loginButton.setPrefHeight(38);

        loginButton.setStyle(
            "-fx-background-color: #28a745;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;" 
        );
     
        loginButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { loginButton.setBackground(new Background(new BackgroundFill(Color.web("#1e7e34"), new CornerRadii(5.00), null))); });
        loginButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { loginButton.setBackground(new Background(new BackgroundFill(Color.web("#28a745"), new CornerRadii(5.00), null))); });


        Button signUpButton = new Button("Sign Up");
        signUpButton.setFont(loadedFont != null ? Font.font(loadedFont.getFamily(), FontWeight.BOLD, 15) : Font.font("System", FontWeight.BOLD, 15));
        signUpButton.setPrefWidth(200); 
        signUpButton.setPrefHeight(38);

        signUpButton.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        signUpButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { signUpButton.setBackground(new Background(new BackgroundFill(Color.web("#4e555b"), new CornerRadii(5.00), null))); });
        signUpButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { signUpButton.setBackground(new Background(new BackgroundFill(Color.web("#6c757d"), new CornerRadii(5.00), null))); });


        loginForm.getChildren().addAll(
                titleLabel,
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                errorMessageLabel,
                loginButton,
                signUpButton
        );

        root.setCenter(loginForm);
        BorderPane.setAlignment(loginForm, Pos.CENTER); 
        BorderPane.setMargin(loginForm, new Insets(50)); 


        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorMessageLabel.setText("Please enter both username and password.");
                errorMessageLabel.setVisible(true);
            } else {
                errorMessageLabel.setVisible(false);
                System.out.println("Login attempt - Username: '" + username + "', Password: '" + password + "'");
                // REMEMBER TO Replace with actual authentication logic to firebase BEFORE DELIVERABLE
                if (username.equals("admin") && password.equals("admin")) { 
                    System.out.println("Login Successful! Navigating to Menu Page...");

                    
                    MenuPage menuPage = new MenuPage();
                    try {
                        
                        menuPage.init();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Could not initialize MenuPage due to an error.");
                        return; 
                    }
                    BorderPane newRoot = menuPage.createMenuPane(primaryStage);

                    Scene menuScene = new Scene(newRoot, 800, 600);

                    primaryStage.setScene(menuScene);

                    primaryStage.setTitle("Blackjack Menu");

                } else {
                    errorMessageLabel.setText("Invalid username or password.");
                    errorMessageLabel.setVisible(true);
                }
            }
        });

        signUpButton.setOnAction(event -> {
            System.out.println("Sign Up button clicked. Implement navigation to registration screen.");
            
        });



        Scene scene = new Scene(root, 784, 388); 
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}