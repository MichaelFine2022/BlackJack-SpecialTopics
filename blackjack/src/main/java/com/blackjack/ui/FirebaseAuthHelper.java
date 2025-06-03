package com.blackjack.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import static com.google.firebase.cloud.FirestoreClient.getFirestore;

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
    
    public static void createUser(String username, String password) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password); 

        db.collection("users").document(username).set(userData).get();

    }

    public static boolean userExists(String username) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection("users").document(username).get().get();
        return snapshot.exists();
    }

    public static boolean validateLogin(String username, String password) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference userRef = db.collection("users").document(username);
        DocumentSnapshot snapshot = userRef.get().get();
        
        if (!snapshot.exists()) return false;

        String storedPassword = snapshot.getString("password");
        System.out.println("Entered username: " + username);
        System.out.println("Entered password: " + password);
        System.out.println("Stored password: " + storedPassword);
        return storedPassword != null && storedPassword.equals(password);
    }
}
