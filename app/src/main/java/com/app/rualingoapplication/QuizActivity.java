package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private TextView quizStatusText, quizDetailsText;
    private MaterialButton startQuizButton;
    private ApiService apiService;
    private List<Question> quizQuestions = new ArrayList<>();
    private String selectedLanguage;
    private Long selectedCourseId = -1L;
    private final List<Long> validLessonIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SessionManager sessionManager = new SessionManager(this);
        selectedLanguage = sessionManager.getSelectedLanguage();
        
        TextView titleTv = findViewById(R.id.quizTitle);
        if (titleTv != null) {
            titleTv.setText(getString(R.string.exercises_title_format, selectedLanguage));
        }

        quizStatusText = findViewById(R.id.quizStatusText);
        quizDetailsText = findViewById(R.id.quizDetailsText);
        startQuizButton = findViewById(R.id.startQuizButton);

        apiService = RetrofitClient.getApiService();

        setupBottomNav();
        fetchLanguageAndLessons();

        startQuizButton.setOnClickListener(v -> {
            if (!quizQuestions.isEmpty()) {
                Intent intent = new Intent(QuizActivity.this, QuestionActivity.class);
                intent.putExtra("questions", (Serializable) quizQuestions);
                startActivity(intent);
            }
        });
    }

    private void fetchLanguageAndLessons() {
        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Course course : response.body()) {
                        if (selectedLanguage != null && course.getTitle() != null &&
                                selectedLanguage.trim().equalsIgnoreCase(course.getTitle().trim())) {
                            selectedCourseId = course.getId();
                            break;
                        }
                    }
                    fetchLessons();
                } else {
                    loadQuizContent();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                loadQuizContent();
            }
        });
    }

    private void fetchLessons() {
        apiService.getLessons().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Lesson>> call, @NonNull Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    validLessonIds.clear();
                    for (Lesson lesson : response.body()) {
                        if (Objects.equals(lesson.getCourseId(), selectedCourseId)) {
                            validLessonIds.add(lesson.getId());
                        }
                    }
                }
                loadQuizContent();
            }
            @Override
            public void onFailure(@NonNull Call<List<Lesson>> call, @NonNull Throwable t) {
                loadQuizContent();
            }
        });
    }

    private void loadQuizContent() {
        apiService.getQuizzes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Question> filtered = filterQuestions(response.body());
                    if (!filtered.isEmpty()) {
                        displayQuizReady(filtered);
                    } else {
                        displayEmptyState();
                    }
                } else {
                    displayEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                displayEmptyState();
            }
        });
    }

    private void displayEmptyState() {
        quizStatusText.setText(R.string.no_exercises_available);
        quizDetailsText.setText("Please check back later once your instructor has published new content.");
        startQuizButton.setEnabled(false);
        startQuizButton.setAlpha(0.5f);
        quizQuestions.clear();
    }

    private List<Question> filterQuestions(List<Question> raw) {
        List<Question> filtered = new ArrayList<>();
        
        // If we haven't identified the course/lessons yet, we can't filter correctly.
        // We should wait or use text-based filtering as a last resort.
        if (validLessonIds.isEmpty() && selectedCourseId != -1L) {
             return filtered; 
        }

        for (Question q : raw) {
            if (q.getLessonId() != null && validLessonIds.contains(q.getLessonId())) {
                filtered.add(q);
            }
        }
        
        // Text-based fallback for mixed databases
        if (filtered.isEmpty() && selectedLanguage != null) {
            String target = selectedLanguage.toLowerCase();
            for (Question q : raw) {
                String text = (q.getPrompt() + " " + q.getAnswer()).toLowerCase();
                if (text.contains(target)) {
                    filtered.add(q);
                }
            }
        }

        return filtered;
    }

    private void displayQuizReady(List<Question> questions) {
        quizQuestions = questions;
        quizStatusText.setText(R.string.exercises_are_ready);
        SessionManager sessionManager = new SessionManager(this);
        String lang = sessionManager.getSelectedLanguage();
        quizDetailsText.setText(getString(R.string.exercise_questions_count_format_lang, questions.size(), lang));
        startQuizButton.setEnabled(true);
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
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}
