package com.khatabook.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private static FirebaseApp firebaseApp;
    private static final String FIREBASE_CONFIG_PATH = "firebase-service-account.json";

    public static void initialize() {
        if (firebaseApp == null) {
            try {
                InputStream serviceAccount = FirebaseConfig.class
                    .getClassLoader()
                    .getResourceAsStream(FIREBASE_CONFIG_PATH);

                if (serviceAccount == null) {
                    throw new IllegalStateException("Firebase configuration file not found: " + FIREBASE_CONFIG_PATH);
                }

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

                firebaseApp = FirebaseApp.initializeApp(options);
                logger.info("Firebase has been initialized successfully");
            } catch (IOException e) {
                logger.error("Error initializing Firebase: {}", e.getMessage());
                throw new RuntimeException("Error initializing Firebase", e);
            }
        }
    }

    public static FirebaseToken verifyToken(String idToken) {
        try {
            if (firebaseApp == null) {
                initialize();
            }
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage());
            throw new RuntimeException("Error verifying Firebase token", e);
        }
    }

    public static String getUserId(String idToken) {
        FirebaseToken decodedToken = verifyToken(idToken);
        return decodedToken.getUid();
    }

    public static String getUserPhone(String idToken) {
        FirebaseToken decodedToken = verifyToken(idToken);
        return decodedToken.getClaims().get("phone_number", String.class);
    }

    public static void shutdown() {
        if (firebaseApp != null) {
            firebaseApp.delete();
            firebaseApp = null;
            logger.info("Firebase has been shut down successfully");
        }
    }
}
