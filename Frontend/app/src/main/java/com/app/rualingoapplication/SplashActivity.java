package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide ActionBar if exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Show splash for 2.5 seconds, then decide where to go
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(this);
            Intent intent;

            android.util.Log.d("SplashActivity", "Deciding navigation. LoggedIn: " + sessionManager.isLoggedIn() + ", Role: " + sessionManager.getRole());

            if (sessionManager.isLoggedIn()) {
                // If already logged in, go to correct dashboard
                if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
                    android.util.Log.d("SplashActivity", "Navigating to AdminActivity");
                    intent = new Intent(SplashActivity.this, AdminActivity.class);
                } else if (sessionManager.isNewUser()) {
                    android.util.Log.d("SplashActivity", "Navigating to LanguageSelectionActivity (New User)");
                    intent = new Intent(SplashActivity.this, LanguageSelectionActivity.class);
                } else {
                    android.util.Log.d("SplashActivity", "Navigating to HomeActivity");
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                }
            } else {
                // Otherwise, go to Welcome screen (MainActivity)
                android.util.Log.d("SplashActivity", "Navigating to MainActivity");
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }

            try {
                startActivity(intent);
                finish();
            } catch (Exception e) {
                android.util.Log.e("SplashActivity", "Failed to start activity", e);
            }
        }, 2500);
    }
}
