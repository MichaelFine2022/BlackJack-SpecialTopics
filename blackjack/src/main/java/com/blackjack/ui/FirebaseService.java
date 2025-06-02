package com.blackjack.ui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

import javafx.application.Platform;

public class FirebaseService {
    private static final String FIREBASE_WEB_API_KEY = "AIzaSyApHH7j9K6KnPEpRcgUvU80bJ1gfqRbGjM";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    
    public interface AuthCallback {
        void onSuccess(String idToken, String refreshToken, String userId);
        void onFailure(String errorMessage);
    }

    
    public void signIn(String email, String password, AuthCallback callback) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_WEB_API_KEY;

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("returnSecureToken", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JSONObject jsonResponse = new JSONObject(response.body());
                        String idToken = jsonResponse.getString("idToken");
                        String refreshToken = jsonResponse.getString("refreshToken");
                        String userId = jsonResponse.getString("localId");
                        System.out.println("FirebaseService: Login successful for user ID: " + userId);
                        Platform.runLater(() -> callback.onSuccess(idToken, refreshToken, userId));
                    } else {
                        JSONObject errorResponse = new JSONObject(response.body());
                        final String[] errorMessage = new String[] { "Unknown error" };
                        if (errorResponse.has("error") && errorResponse.getJSONObject("error").has("message")) {
                            errorMessage[0] = errorResponse.getJSONObject("error").getString("message");
                        }
                        Platform.runLater(() -> callback.onFailure(errorMessage[0]));

                    }
                })
                .exceptionally(e -> {
                    System.err.println("FirebaseService: Network error during login: " + e.getMessage());
                    Platform.runLater(() -> callback.onFailure("Network error: " + e.getMessage()));
                    return null;
                });
    }

    public void signUp(String email, String password, AuthCallback callback) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + FIREBASE_WEB_API_KEY;

        JSONObject requestBody = new JSONObject();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("returnSecureToken", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JSONObject jsonResponse = new JSONObject(response.body());
                        String idToken = jsonResponse.getString("idToken");
                        String refreshToken = jsonResponse.getString("refreshToken");
                        String userId = jsonResponse.getString("localId");
                        System.out.println("FirebaseService: Sign-up successful for user ID: " + userId);
                        Platform.runLater(() -> callback.onSuccess(idToken, refreshToken, userId));
                    } else {
                        JSONObject errorResponse = new JSONObject(response.body());
                        final String[] errorMessage = new String[] { "Unknown error" };
                        if (errorResponse.has("error") && errorResponse.getJSONObject("error").has("message")) {
                            errorMessage[0] = errorResponse.getJSONObject("error").getString("message");
                        }
                        Platform.runLater(() -> callback.onFailure(errorMessage[0]));
                    }
                })
                .exceptionally(e -> {
                    System.err.println("FirebaseService: Network error during sign-up: " + e.getMessage());
                    Platform.runLater(() -> callback.onFailure("Network error: " + e.getMessage()));
                    return null;
                });
    }

    // https://firebase.google.com/docs/reference/rest/auth#section-refresh-token
    public void refreshIdToken(String refreshToken, AuthCallback callback) {
        String url = "https://securetoken.googleapis.com/v1/token?key=" + FIREBASE_WEB_API_KEY;

        JSONObject requestBody = new JSONObject();
        requestBody.put("grant_type", "refresh_token");
        requestBody.put("refresh_token", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JSONObject jsonResponse = new JSONObject(response.body());
                        String newIdToken = jsonResponse.getString("id_token"); 
                        String newRefreshToken = jsonResponse.getString("refresh_token"); 
                        String userId = jsonResponse.getString("user_id"); 
                        System.out.println("FirebaseService: Token refreshed for user ID: " + userId);
                        Platform.runLater(() -> callback.onSuccess(newIdToken, newRefreshToken, userId));
                    } else {
                        JSONObject errorResponse = new JSONObject(response.body());
                        final String[] errorMessage = new String[] { "Unknown error" };
                        if (errorResponse.has("error") && errorResponse.getJSONObject("error").has("message")) {
                            errorMessage[0] = errorResponse.getJSONObject("error").getString("message");
                        }
                        Platform.runLater(() -> callback.onFailure(errorMessage[0]));

                    }
                })
                .exceptionally(e -> {
                    System.err.println("FirebaseService: Network error during token refresh: " + e.getMessage());
                    Platform.runLater(() -> callback.onFailure("Network error during token refresh: " + e.getMessage()));
                    return null;
                });
    }
}