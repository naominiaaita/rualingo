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
        String role = BuildConfig.FLAVOR_TYPE;
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
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleAuthSuccess(response.body());
                } else if (response.code() == 409) {
                    // Account already exists; try logging in with the same credentials.
                    Log.d("SignupActivity", "Conflict 409: account exists. Attempting auto-login...");
                    attemptAutoLogin(email, password);
                } else {
                    String error = "Registration failed (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) error += ": " + response.errorBody().string();
                    } catch (Exception ignored) {
                    }
                    Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show();
                    Log.e("SignupActivity", error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptAutoLogin(String email, String password) {
        setLoading(true);
        String role = BuildConfig.FLAVOR_TYPE;
        User user = new User(email, email, password, role);

        apiService.login(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleAuthSuccess(response.body());
                } else {
                    showCollisionDialog(email);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                showCollisionDialog(email);
            }
        });
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
