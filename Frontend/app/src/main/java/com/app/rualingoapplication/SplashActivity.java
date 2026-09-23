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

        // Show splash for 2.5 seconds, then go to MainActivity (landing screen / routing)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            try {
                startActivity(intent);
                finish();
            } catch (Exception e) {
                android.util.Log.e("SplashActivity", "Failed to start activity", e);
            }
        }, 2500);
    }
}
