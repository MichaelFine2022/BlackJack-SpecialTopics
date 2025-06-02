package com.blackjack.ui;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

public class FirebaseAuthHelper {
    private static boolean initialized = false;

    public static void initializeFirebase() throws IOException {
        if (initialized) return;

        FileInputStream serviceAccount = new FileInputStream("blackjack/src/main/resources/firebaseServiceAccount.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        initialized = true;
    }

    public static boolean userExists(String email) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            return userRecord != null;
        } catch (Exception e) {
            return false;
        }
    }
}
