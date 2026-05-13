package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class LanguageSelectionActivity extends AppCompatActivity {

    private MaterialButton btnContinue;
    private MaterialButton selectedButton = null;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();
        
        // SECURITY CHECK: Admins should not be in the language selection screen
        if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
            Log.d("LanguageSelection", "Admin user detected. Redirecting to Admin Dashboard.");
            Intent intent = new Intent(this, AdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_language_select);

        btnContinue = findViewById(R.id.btnContinue);
        
        setupLanguageButton(R.id.langMotu, "Motu");
        setupLanguageButton(R.id.langTokPisin, "Tok Pisin");

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(LanguageSelectionActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupLanguageButton(int id, String languageName) {
        MaterialButton btn = findViewById(id);
        if (btn == null) {
            return;
        }
        btn.setOnClickListener(v -> {
            if (selectedButton != null) {
                selectedButton.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E5E5E5")));
            }
            btn.setStrokeColor(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.duo_blue)));
            selectedButton = btn;
            sessionManager.setSelectedLanguage(languageName);
            syncLanguageToBackend(languageName);
            btnContinue.setEnabled(true);
        });
    }

    private void syncLanguageToBackend(String languageName) {
        Long userId = sessionManager.getUserId();
        if (userId == -1) return;

        User update = new User();
        update.setCurrentCourse(languageName);
        
        apiService.editUser(userId, update).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<User> call, @androidx.annotation.NonNull retrofit2.Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d("LanguageSelection", "Language synced to backend: " + languageName);
                }
            }
            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<User> call, @androidx.annotation.NonNull Throwable t) {
                Log.e("LanguageSelection", "Failed to sync language", t);
            }
        });
    }
}
