package com.blackjack.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class SignUp {

    private Font loadedFont;

    public SignUp() {
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

    public void show(Stage primaryStage) {
        primaryStage.setTitle("Blackjack - Sign Up");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");

        VBox signUpForm = new VBox(15);
        signUpForm.setAlignment(Pos.CENTER);
        signUpForm.setPadding(new Insets(30));
        signUpForm.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #d3d3d3;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
        signUpForm.setMaxWidth(350);

        Label titleLabel = new Label("Create Your Account");
        
        titleLabel.setStyle("-fx-text-fill: #333333;");

        Label usernameLabel = new Label("Preferred Username");
        usernameLabel.setStyle("-fx-text-fill: #555555;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your preferred username");
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

        passwordLabel.setStyle("-fx-text-fill: #555555;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

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

        Label errorLabel = new Label();
        
        errorLabel.setTextFill(Color.web("#dc3545"));
        errorLabel.setVisible(false);

        Button createAccountButton = new Button("Sign Up");

        createAccountButton.setPrefWidth(200);
        createAccountButton.setPrefHeight(38);
        createAccountButton.setStyle(
            "-fx-background-color: #28a745;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );

        createAccountButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e ->
            createAccountButton.setBackground(new Background(new BackgroundFill(Color.web("#1e7e34"), new CornerRadii(5.00), null)))
        );
        createAccountButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e ->
            createAccountButton.setBackground(new Background(new BackgroundFill(Color.web("#28a745"), new CornerRadii(5.00), null)))
        );

        Button backToLoginButton = new Button("Back to Login");
        
        backToLoginButton.setPrefWidth(200);
        backToLoginButton.setPrefHeight(38);
        backToLoginButton.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );

        backToLoginButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e ->
            backToLoginButton.setBackground(new Background(new BackgroundFill(Color.web("#4e555b"), new CornerRadii(5.00), null)))
        );
        backToLoginButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e ->
            backToLoginButton.setBackground(new Background(new BackgroundFill(Color.web("#6c757d"), new CornerRadii(5.00), null)))
        );

        createAccountButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty()) {
                errorLabel.setText("Please enter a username.");
                errorLabel.setVisible(true);
                return;
            }
        
            try {
                FirebaseAuthHelper.initializeFirebase();
        
                if (FirebaseAuthHelper.userExists(username)) {
                    errorLabel.setText("Username already taken.");
                    errorLabel.setVisible(true);
                } else {
                    FirebaseAuthHelper.createUser(username, password);
                    errorLabel.setText("Account created successfully!");
                    errorLabel.setTextFill(Color.web("#28a745"));
                    errorLabel.setVisible(true);
                    new Login().start(primaryStage);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Sign-up failed: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        backToLoginButton.setOnAction(e -> {
            new Login().start(primaryStage);
        });

        signUpForm.getChildren().addAll(
            titleLabel,
            usernameLabel,
            usernameField,
            passwordLabel,
            passwordField,
            errorLabel,
            createAccountButton,
            backToLoginButton
        );


        root.setCenter(signUpForm);
        BorderPane.setAlignment(signUpForm, Pos.CENTER);
        BorderPane.setMargin(signUpForm, new Insets(50));

        Scene scene = new Scene(root, 784, 388);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
