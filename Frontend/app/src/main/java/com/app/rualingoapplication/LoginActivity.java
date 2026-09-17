package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText identifierEditText, passwordEditText;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        identifierEditText = findViewById(R.id.identifierEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton loginSubmitButton = findViewById(R.id.loginSubmitButton);
        android.widget.TextView signupLink = findViewById(R.id.signupLink);

        // Adjust UI for Flavor
        if ("Admin".equalsIgnoreCase(BuildConfig.FLAVOR_TYPE)) {
            setTitle("Admin Login");
            if (signupLink != null) signupLink.setText("ADMIN REGISTRATION");
        } else {
            setTitle("Learner Login");
        }

        apiService = RetrofitClient.getApiService();

        backButton.setOnClickListener(v -> finish());

        if (signupLink != null) {
            signupLink.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        loginSubmitButton.setOnClickListener(v -> {
            String identifier = Objects.requireNonNull(identifierEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

            String role = BuildConfig.FLAVOR_TYPE;

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(identifier, password, role);
        });
    }

    private void loginUser(final String identifier, final String password, String role) {
        User user = new User(identifier, identifier, password, role);
        apiService.login(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body(), password, identifier);
                } else if (response.code() == 401 || response.code() == 404) {
                    // Potential desync: Check if Firebase has the user
                    Log.w("LoginActivity", "Backend login failed. Checking Firebase for desync...");
                    checkFirebaseDesync(identifier, password, role);
                } else {
                    showDetailedError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e("LoginActivity", "Login failure", t);
                Toast.makeText(LoginActivity.this, getString(R.string.network_error) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkFirebaseDesync(String identifier, String password, String role) {
        String firebaseEmail = identifier.contains("@") ? identifier : "";
        if (firebaseEmail.isEmpty()) {
            Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(firebaseEmail, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("LoginActivity", "Firebase success but backend failed. Re-syncing account...");
                    Toast.makeText(this, "Re-syncing account with server...", Toast.LENGTH_SHORT).show();
                    syncMissingUser(identifier, firebaseEmail, password, role);
                } else {
                    Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void syncMissingUser(String username, String email, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setFirstName(username);
        user.setSecondName("User");
        user.setGender("Other");
        user.setDateOfBirth("2000-01-01");
        user.setProvinceOfOrigin("NCD");

        apiService.signup(user).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body(), password, email);
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to re-sync account. Please try registering again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Connection error during sync.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDetailedError(Response<AuthResponse> response) {
        String errorMsg = getString(R.string.invalid_credentials);
        try {
            if (response.errorBody() != null) {
                String serverError = response.errorBody().string();
                Log.e("LoginActivity", "Server error: " + serverError);
                errorMsg += " (" + response.code() + ")";
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error reading error body", e);
        }
        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void handleLoginSuccess(AuthResponse authResponse, String password, String identifier) {
        // Verify if the login matches the expected flavor
        String actualRole = authResponse.getRole();
        if ("Admin".equalsIgnoreCase(BuildConfig.FLAVOR_TYPE) && !"Admin".equalsIgnoreCase(actualRole)) {
            Toast.makeText(LoginActivity.this, "This account is not an admin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract JWT token from response body
        String token = authResponse.getToken();

        // Create User object for session from AuthResponse
        User loggedInUser = new User();
        loggedInUser.setId(authResponse.getUserId());
        loggedInUser.setUsername(authResponse.getUsername());
        loggedInUser.setEmail(authResponse.getEmail());
        loggedInUser.setRole(actualRole);
        loggedInUser.setStreak(authResponse.getStreak());
        
        sessionManager.createLoginSession(loggedInUser, token);
        Toast.makeText(LoginActivity.this, getString(R.string.welcome_back_format, authResponse.getUsername()), Toast.LENGTH_SHORT).show();
        
        // Sync with Firebase Authentication to resolve desync
        String firebaseEmail = authResponse.getEmail() != null && !authResponse.getEmail().isEmpty() 
                ? authResponse.getEmail() : (identifier.contains("@") ? identifier : "");
        if (!firebaseEmail.isEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(firebaseEmail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "Firebase Authentication successful sync.");
                    } else {
                        Log.e("LoginActivity", "Firebase Authentication failed sync.", task.getException());
                    }
                });
        }

        Intent intent;
        if ("ADMIN".equalsIgnoreCase(actualRole)) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else if (authResponse.isNewUser()) {
            intent = new Intent(LoginActivity.this, LanguageSelectionActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
