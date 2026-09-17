package com.app.rualingoapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LanguageSelectionActivity extends AppCompatActivity {

    private MaterialButton btnContinue;
    private SessionManager sessionManager;
    private ApiService apiService;
    private LanguageDetailAdapter adapter;
    private android.widget.ProgressBar progressBar;
    private android.widget.TextView tvEmpty;
    private final List<LanguageModel> languagesList = new ArrayList<>();
    private LanguageModel selectedLanguage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();
        
        if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_language_select);

        btnContinue = findViewById(R.id.btnContinue);
        progressBar = findViewById(R.id.languageProgressBar);
        tvEmpty = findViewById(R.id.tvEmptyLanguages);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.languageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LanguageDetailAdapter(languagesList, language -> {
            selectedLanguage = language;
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
            adapter.notifyDataSetChanged(); // Refresh to update radio buttons
        });
        recyclerView.setAdapter(adapter);

        loadLanguages();
        setupBottomNav();

        btnContinue.setOnClickListener(v -> {
            if (selectedLanguage != null) {
                String langName = selectedLanguage.getName() != null ? selectedLanguage.getName() : selectedLanguage.getLanguageName();
                sessionManager.setSelectedLanguage(langName);
                syncLanguageToBackend(langName);
            }
        });
    }

    private void loadLanguages() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        Log.d("LanguageSelect", "Loading languages from API: " + BuildConfig.BASE_URL + "api/languages");
        apiService.getLanguages().enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<LanguageModel>> call, @NonNull Response<List<LanguageModel>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.d("LanguageSelect", "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    languagesList.clear();
                    languagesList.addAll(response.body());
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    fetchExtraStats();
                    Log.d("LanguageSelect", "Loaded " + languagesList.size() + " languages");
                } else {
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                    String error = "No languages found or server error (" + response.code() + ")";
                    Log.w("LanguageSelect", error);
                    Toast.makeText(LanguageSelectionActivity.this, error, Toast.LENGTH_LONG).show();
                    // No mock fallback
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LanguageModel>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvEmpty != null && languagesList.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                String error = "Failed to load languages: " + t.getMessage();
                Log.e("LanguageSelect", error);
                Toast.makeText(LanguageSelectionActivity.this, error, Toast.LENGTH_LONG).show();
                // No mock fallback
            }
        });
    }

    private void fetchExtraStats() {
        // Fetch courses and lessons to calculate counts for cards
        apiService.getCourses().enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();
                    apiService.getLessons(null).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Lesson>> call, @NonNull Response<List<Lesson>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Lesson> lessons = response.body();
                                updateLanguageStats(courses, lessons);
                            }
                        }
                        @Override public void onFailure(@NonNull Call<List<Lesson>> call, @NonNull Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {}
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateLanguageStats(List<Course> courses, List<Lesson> lessons) {
        for (LanguageModel lang : languagesList) {
            int cCount = 0, lCount = 0;
            for (Course c : courses) {
                if (c.getLanguageId() != null && c.getLanguageId().equals(lang.getId())) {
                    cCount++;
                    for (Lesson l : lessons) {
                        if (l.getCourseId() != null && l.getCourseId().equals(c.getId())) {
                            lCount++;
                        }
                    }
                }
            }
            lang.setCourseCount(cCount);
            lang.setLessonCount(lCount);
        }
        adapter.notifyDataSetChanged();
    }

    private void syncLanguageToBackend(String language) {
        Long userId = sessionManager.getUserId();
        
        // Progress immediately to UI if user is guest or we have no network
        if (userId == -1) {
            navigateToNext();
            return;
        }

        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Course targetCourse = null;
                    for (Course c : response.body()) {
                        String cLang = c.getLanguageName() != null ? c.getLanguageName() : "";
                        String cTitle = c.getTitle() != null ? c.getTitle() : "";
                        if (language.equalsIgnoreCase(cLang) || language.equalsIgnoreCase(cTitle)) {
                            targetCourse = c;
                            break;
                        }
                    }

                    if (targetCourse != null) {
                        enrollUserInCourse(targetCourse, language);
                    } else {
                        // If no specific course matches, try to enroll in the first one as fallback 
                        // so the Home screen isn't empty
                        if (!response.body().isEmpty()) {
                            enrollUserInCourse(response.body().get(0), language);
                        } else {
                            updateUserProfileLegacy(userId, language);
                        }
                    }
                } else {
                    updateUserProfileLegacy(userId, language);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                // Network failed? Just move on, HomeActivity will handle the offline/missing state
                updateUserProfileLegacy(userId, language);
            }
        });
    }

    private void enrollUserInCourse(Course course, String languageName) {
        apiService.enrollInCourse(course.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                sessionManager.setSelectedCourseId(course.getId());
                updateUserProfileLegacy(sessionManager.getUserId(), languageName);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                updateUserProfileLegacy(sessionManager.getUserId(), languageName);
            }
        });
    }

    private void updateUserProfileLegacy(Long userId, String language) {
        User update = new User();
        update.setCurrentCourse(language);
        
        apiService.editUser(userId, update).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.updateProfile(response.body());
                }
                navigateToNext();
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                navigateToNext();
            }
        });
    }

    private void navigateToNext() {
        startActivity(new Intent(LanguageSelectionActivity.this, OnboardingActivity.class));
        finish();
    }

    private void setupBottomNav() {
        View learn = findViewById(R.id.navLearn);
        if (learn != null) learn.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        View vocab = findViewById(R.id.navVocabulary);
        if (vocab != null) vocab.setOnClickListener(v -> {
            startActivity(new Intent(this, VocabularyActivity.class));
            finish();
        });
        View profile = findViewById(R.id.navProfile);
        if (profile != null) profile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    // Helper to check if language is selected for RadioButton logic in Adapter if needed
    public LanguageModel getSelectedLanguage() {
        return selectedLanguage;
    }
}
