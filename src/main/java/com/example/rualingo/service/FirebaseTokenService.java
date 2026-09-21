package com.example.rualingo.service;

import com.example.rualingo.config.AuthProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class FirebaseTokenService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenService(AuthProperties authProperties) {
        String encodedCredentials = authProperties.getFirebase().getServiceAccountJsonBase64();
        if (encodedCredentials == null || encodedCredentials.isBlank()) {
            firebaseAuth = null;
            return;
        }

        try {
            byte[] credentialsJson = Base64.getDecoder().decode(encodedCredentials.replaceAll("\\s", ""));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(credentialsJson)))
                    .build();
            FirebaseApp app = FirebaseApp.getApps().stream().findFirst()
                    .orElseGet(() -> FirebaseApp.initializeApp(options));
            firebaseAuth = FirebaseAuth.getInstance(app);
        } catch (IOException | IllegalArgumentException ex) {
            throw new IllegalStateException("Firebase Admin SDK could not be initialized.", ex);
        }
    }

    public FirebaseToken requireVerifiedEmail(String idToken, String expectedEmail) {
        if (firebaseAuth == null) {
            throw new IllegalStateException("Firebase Admin SDK is not configured.");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Firebase ID token is required.");
        }

        try {
            FirebaseToken token = firebaseAuth.verifyIdToken(idToken);
            Object emailVerified = token.getClaims().get("email_verified");
            String tokenEmail = token.getEmail();
            if (!Boolean.TRUE.equals(emailVerified)) {
                throw new IllegalArgumentException("Email must be verified in Firebase.");
            }
            if (tokenEmail == null || expectedEmail == null ||
                    !tokenEmail.equalsIgnoreCase(expectedEmail.trim())) {
                throw new IllegalArgumentException("Firebase email does not match the account email.");
            }
            return token;
        } catch (FirebaseAuthException ex) {
            throw new IllegalArgumentException("Invalid Firebase ID token.", ex);
        }
    }
}