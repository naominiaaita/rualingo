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
        User loggedInUser = new User();
        loggedInUser.setId(System.currentTimeMillis()); // Mock ID
        loggedInUser.setUsername(identifier.contains("@") ? identifier.split("@")[0] : identifier);
        loggedInUser.setEmail(identifier.contains("@") ? identifier : identifier + "@mock.com");
        loggedInUser.setRole(role);
        loggedInUser.setFirstName(loggedInUser.getUsername());
        loggedInUser.setSecondName("User");
        loggedInUser.setStreak(0);

        sessionManager.createLoginSession(loggedInUser, "mock-token-" + System.currentTimeMillis());
        Toast.makeText(LoginActivity.this, "Login Bypass Successful", Toast.LENGTH_SHORT).show();

        Intent intent;
        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
