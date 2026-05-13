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

            if (sessionManager.isLoggedIn()) {
                // If already logged in, go to correct dashboard
                if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
                    intent = new Intent(SplashActivity.this, AdminActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                }
            } else {
                // Otherwise, go to Welcome screen (MainActivity)
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2500);
    }
}
