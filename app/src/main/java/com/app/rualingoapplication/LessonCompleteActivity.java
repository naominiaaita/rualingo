package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class LessonCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_complete);

        SessionManager sessionManager = new SessionManager(this);
        
        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);
        
        // Award XP and update stats
        int earnedXp = score * 10;
        sessionManager.addXP(earnedXp);
        sessionManager.recordPerformance(score, total);

        TextView completionSubtitle = findViewById(R.id.completionSubtitle);
        String summary = "You got " + score + "/" + total + " correct!\nYou earned " + earnedXp + " XP.";
        completionSubtitle.setText(summary);

        MaterialButton finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
