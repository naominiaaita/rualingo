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

            if (!sessionManager.isLoggedIn()) {
                // Automatically create a mock logged-in session to completely bypass authentication screens
                User mockUser = new User();
                mockUser.setId(12345L);
                mockUser.setUsername("GuestLearner");
                mockUser.setEmail("guest@rualingo.com");
                mockUser.setFirstName("Guest");
                mockUser.setSecondName("Learner");
                mockUser.setStreak(1);
                
                String flavorRole = BuildConfig.FLAVOR_TYPE;
                if ("Admin".equalsIgnoreCase(flavorRole)) {
                    mockUser.setRole("ADMIN");
                } else {
                    mockUser.setRole("USER");
                }
                
                sessionManager.createLoginSession(mockUser, "mock-bypass-token");
                sessionManager.setNewUser(false); // Bypass language selection if desired, or set to true if preferred
            }

            android.util.Log.d("SplashActivity", "Deciding navigation. LoggedIn: " + sessionManager.isLoggedIn() + ", Role: " + sessionManager.getRole());

            if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
                android.util.Log.d("SplashActivity", "Navigating to AdminActivity");
                intent = new Intent(SplashActivity.this, AdminActivity.class);
            } else {
                android.util.Log.d("SplashActivity", "Navigating to HomeActivity");
                intent = new Intent(SplashActivity.this, HomeActivity.class);
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
