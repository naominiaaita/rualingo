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
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, emailEditText, passwordEditText;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton loginSubmitButton = findViewById(R.id.loginSubmitButton);

        // Adjust UI for Flavor
        if ("ADMIN".equals(BuildConfig.FLAVOR_TYPE)) {
            setTitle("Admin Login");
        } else {
            setTitle("Learner Login");
        }

        apiService = RetrofitClient.getApiService();

        backButton.setOnClickListener(v -> finish());

        loginSubmitButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
            String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

            String role = BuildConfig.FLAVOR_TYPE;

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, email, password, role);
        });
    }

    private void loginUser(String username, String email, String password, String role) {
        User user = new User(username, email, password, role);
        apiService.login(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Verify if the login matches the expected flavor
                    String actualRole = authResponse.getRole();
                    if ("ADMIN".equals(BuildConfig.FLAVOR_TYPE) && !"ADMIN".equalsIgnoreCase(actualRole)) {
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
                    
                    sessionManager.createLoginSession(loggedInUser, token);
                    Toast.makeText(LoginActivity.this, getString(R.string.welcome_back_format, authResponse.getUsername()), Toast.LENGTH_SHORT).show();
                    
                    Intent intent;
                    if ("ADMIN".equalsIgnoreCase(actualRole)) {
                        intent = new Intent(LoginActivity.this, AdminActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                    }

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
