package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, emailEditText, passwordEditText;
    private MaterialButton signupSubmitButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        RetrofitClient.setContext(this);
        apiService = RetrofitClient.getApiService();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupSubmitButton = findViewById(R.id.signupSubmitButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        signupSubmitButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
            String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            setLoading(true);
            performRegistration(username, email, password);
        });
    }

    private void setLoading(boolean isLoading) {
        signupSubmitButton.setEnabled(!isLoading);
        signupSubmitButton.setText(isLoading ? "PROCESSING..." : "CREATE ACCOUNT");
        signupSubmitButton.setAlpha(isLoading ? 0.7f : 1.0f);
    }

    private void performRegistration(String username, String email, String password) {
        setLoading(false);
        SessionManager sessionManager = new SessionManager(this);
        String role = BuildConfig.FLAVOR_TYPE;

        User loggedInUser = new User();
        loggedInUser.setId(System.currentTimeMillis()); // Mock ID
        loggedInUser.setUsername(username);
        loggedInUser.setEmail(email);
        loggedInUser.setRole(role);
        loggedInUser.setFirstName(username);
        loggedInUser.setSecondName("User");
        loggedInUser.setStreak(0);

        sessionManager.createLoginSession(loggedInUser, "mock-token-" + System.currentTimeMillis());

        Toast.makeText(this, "Registration Bypass Successful", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SignupActivity.this, LanguageSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleAuthSuccess(AuthResponse authResponse) {
        SessionManager sessionManager = new SessionManager(SignupActivity.this);

        User loggedInUser = new User();
        loggedInUser.setId(authResponse.getUserId());
        loggedInUser.setUsername(authResponse.getUsername());
        loggedInUser.setEmail(authResponse.getEmail());
        loggedInUser.setRole(authResponse.getRole());
        loggedInUser.setStreak(authResponse.getStreak());

        sessionManager.createLoginSession(loggedInUser, authResponse.getToken());

        Intent intent;
        if (authResponse.isNewUser()) {
            intent = new Intent(SignupActivity.this, LanguageSelectionActivity.class);
        } else {
            intent = new Intent(SignupActivity.this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showCollisionDialog(String email) {
        new AlertDialog.Builder(this)
            .setTitle("Account Already Exists")
            .setMessage("The email " + email + " is already registered. If you forgot your password, please use the login screen.")
            .setPositiveButton("Go to Login", (dialog, which) -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
