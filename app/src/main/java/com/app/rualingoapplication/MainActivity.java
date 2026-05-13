package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private TextView statusText;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        
        // FORCED REDIRECTION: If logged in, go to the appropriate dashboard immediately
        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getRole();
            if ("ADMIN".equals(BuildConfig.FLAVOR_TYPE) && !"ADMIN".equalsIgnoreCase(role)) {
                sessionManager.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            Intent intent = "ADMIN".equalsIgnoreCase(role)
                    ? new Intent(this, AdminActivity.class)
                    : new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        MaterialButton getStartedButton = findViewById(R.id.getStartedButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);

        // Adjust UI for Admin Flavor
        if ("ADMIN".equals(BuildConfig.FLAVOR_TYPE)) {
            getStartedButton.setText(R.string.admin_registration_btn);
            loginButton.setText(R.string.admin_login_btn);
            TextView tagline = findViewById(R.id.taglineText);
            if (tagline != null) tagline.setText(R.string.admin_portal_tagline);
        }

        apiService = RetrofitClient.getApiService();

        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        fetchQuestions();
    }

    private void fetchQuestions() {
        statusText.setText(R.string.connecting);
        apiService.getExercises().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                if (response.isSuccessful()) {
                    statusText.setText(R.string.ready_to_learn);
                } else {
                    statusText.setText(R.string.server_error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                statusText.setText(R.string.network_error);
                android.util.Log.e("MainActivity", "Connection failed", t);
            }
        });
    }
}
