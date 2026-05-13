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
import java.io.IOException;
import java.util.Objects;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, emailEditText, passwordEditText;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        RetrofitClient.setContext(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton signupSubmitButton = findViewById(R.id.signupSubmitButton);

        apiService = RetrofitClient.getApiService();

        backButton.setOnClickListener(v -> finish());

        signupSubmitButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
            String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(username, email, password);
        });
    }

    private void registerUser(String username, String email, String password) {
        // Map flavor types to backend-expected role names
        final String flavorType = BuildConfig.FLAVOR_TYPE;
        final String backendRole = "ADMIN".equalsIgnoreCase(flavorType) ? "Admin" : "Student";
        
        Log.d("SignupActivity", "Registering user: " + username + " with role: " + backendRole);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(backendRole);
        user.setGender("Other"); 
        user.setFirstName(username); 
        user.setSecondName("User"); 
        user.setDateOfBirth("2000-01-01"); // Required by backend
        user.setProvinceOfOrigin("NCD");   // Required by backend
        
        apiService.signup(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignupActivity.this, R.string.registration_successful, Toast.LENGTH_SHORT).show();
                    AuthResponse authResponse = response.body();
                    SessionManager sessionManager = new SessionManager(SignupActivity.this);

                    String returnedRole = authResponse.getRole();
                    if (returnedRole == null || returnedRole.isEmpty()) {
                        returnedRole = backendRole;
                    }
                    
                    Log.d("SignupActivity", "Final session role: " + returnedRole);

                    User registeredUser = new User();
                    registeredUser.setId(authResponse.getUserId());
                    registeredUser.setUsername(authResponse.getUsername());
                    registeredUser.setEmail(authResponse.getEmail());
                    registeredUser.setRole(returnedRole);
                    sessionManager.createLoginSession(registeredUser, authResponse.getToken());
                    
                    // Force path separation based on flavor and role
                    Intent intent;
                    if ("ADMIN".equalsIgnoreCase(returnedRole)) {
                        Log.d("SignupActivity", "Redirecting to AdminActivity");
                        intent = new Intent(SignupActivity.this, AdminActivity.class);
                    } else {
                        Log.d("SignupActivity", "Redirecting to LanguageSelectionActivity");
                        intent = new Intent(SignupActivity.this, LanguageSelectionActivity.class);
                    }
                    
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    StringBuilder errorMsg = new StringBuilder(getString(R.string.signup_failed) + ": " + response.code());
                    String backendBody = null;
                    try (ResponseBody errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            backendBody = errorBody.string();
                            errorMsg.append(" ").append(backendBody);
                        }
                    } catch (IOException ignored) {}
                    if (backendBody != null && !backendBody.isBlank()) {
                        Log.e("SignupActivity", "Signup 4xx/5xx body: " + backendBody);
                    }
                    Toast.makeText(SignupActivity.this, errorMsg.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(SignupActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                Log.e("SignupActivity", "Error", t);
            }
        });
    }
}
