package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView profileUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);

        profileUsername = findViewById(R.id.profileUsername);
        TextView streakValue = findViewById(R.id.streakValue);
        TextView xpValue = findViewById(R.id.xpValue);
        MaterialButton logoutButton = findViewById(R.id.logoutButton);

        // Populate profile data
        refreshProfileData();
        streakValue.setText(String.format(java.util.Locale.getDefault(), "%d", sessionManager.getStreak()));
        xpValue.setText(String.format(java.util.Locale.getDefault(), "%d", sessionManager.getXP()));

        logoutButton.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNav();
        
        // Settings/Manage Icon
        findViewById(R.id.settingsIcon).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileData();
    }

    private void refreshProfileData() {
        String fullName = sessionManager.getFirstName() + " " + sessionManager.getSecondName();
        if (fullName.trim().isEmpty()) {
            profileUsername.setText(sessionManager.getUsername());
        } else {
            profileUsername.setText(fullName);
        }

        String selectedLanguage = sessionManager.getSelectedLanguage();
        TextView languageText = findViewById(R.id.learningLanguageText);
        android.widget.ImageView flagImage = findViewById(R.id.imgCurrentLanguageFlag);

        if (languageText != null) {
            languageText.setText(getString(R.string.learning_language_format, selectedLanguage));
        }

        if (flagImage != null) {
            if ("Motu".equalsIgnoreCase(selectedLanguage)) {
                flagImage.setImageResource(R.drawable.central_flag);
            } else if ("Tok Pisin".equalsIgnoreCase(selectedLanguage)) {
                flagImage.setImageResource(R.drawable.png_flag);
            } else {
                flagImage.setImageResource(R.drawable.rualingo_logo);
            }
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.navLearn).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navVocabulary).setOnClickListener(v -> {
            startActivity(new Intent(this, VocabularyActivity.class));
            finish();
        });
        // navProfile is already the current activity
    }
}
