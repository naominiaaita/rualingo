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
        Long lessonId = getIntent().getLongExtra("lessonId", -1L);
        
        // Award XP and update stats
        int earnedXp = score * 10;
        sessionManager.addXP(earnedXp);
        sessionManager.recordPerformance(score, total, lessonId);

        TextView completionSubtitle = findViewById(R.id.completionSubtitle);
        int accuracy = total > 0 ? (int) ((score * 100.0) / total) : 0;
        
        String scoreText = getString(R.string.score_summary, score, total);
        String accuracyText = getString(R.string.accuracy_format, accuracy);
        String xpText = getString(R.string.xp_earned_format, earnedXp);
        
        String summary = scoreText + "\n" + accuracyText + "\n" + xpText;
        completionSubtitle.setText(summary);

        MaterialButton finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(v -> {
            // FORCE FIX: Ensure lesson completion is logged on the server
            if (lessonId != -1L) {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("action", "LESSON_COMPLETED");
                payload.put("lessonId", lessonId);
                payload.put("clientTimestamp", System.currentTimeMillis());
                
                RetrofitClient.getApiService().logUserActivity(sessionManager.getUserId(), payload)
                        .enqueue(new retrofit2.Callback<User>() {
                            @Override
                            public void onResponse(retrofit2.Call<User> call, retrofit2.Response<User> response) {
                                android.util.Log.d("LessonComplete", "Explicit completion log success");
                            }
                            @Override
                            public void onFailure(retrofit2.Call<User> call, Throwable t) {
                                android.util.Log.e("LessonComplete", "Explicit completion log failed", t);
                            }
                        });
            }

            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
