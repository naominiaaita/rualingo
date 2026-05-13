package com.app.rualingoapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private List<Question> allQuestions = new ArrayList<>();
    private final List<Lesson> filteredLessons = new ArrayList<>();
    private final List<Long> validLessonIds = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private String selectedLanguage;
    private Long selectedCourseId = -1L;
    private LinearLayout lessonContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        selectedLanguage = sessionManager.getSelectedLanguage();
        Log.d(TAG, "Selected Language from Session: " + selectedLanguage);
        
        if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);
        apiService = RetrofitClient.getApiService();
        lessonContainer = findViewById(R.id.lessonContainer);

        setupTopBar();
        setupBottomNav();
        loadDataFromServer();
        syncOnlineStatus(true);
    }

    private void syncOnlineStatus(boolean isActive) {
        if (sessionManager.getUserId() == -1) return;
        
        User update = new User();
        update.setIsActive(isActive);
        // Backend might require more fields, but we try partial update
        apiService.editUser(sessionManager.getUserId(), update).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Online status synced: " + isActive);
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to sync online status", t);
            }
        });
    }

    private void setupTopBar() {
        TextView streakText = findViewById(R.id.streakCount);
        if (streakText != null) {
            streakText.setText(String.format(java.util.Locale.getDefault(), "%d", sessionManager.getStreak()));
        }
        
        ImageView logoIv = findViewById(R.id.topBarLogo);
        if (logoIv != null) {
            if ("Motu".equalsIgnoreCase(selectedLanguage)) {
                logoIv.setImageResource(R.drawable.central_flag);
            } else if ("Tok Pisin".equalsIgnoreCase(selectedLanguage)) {
                logoIv.setImageResource(R.drawable.png_flag);
            }
        }
    }

    private void loadDataFromServer() {
        Log.d(TAG, "Loading courses from server. Target language: " + selectedLanguage);
        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Courses fetched: " + response.body().size());
                    for (Course course : response.body()) {
                        String title = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
                        String langName = course.getLanguageName() != null ? course.getLanguageName().toLowerCase() : "";
                        String target = selectedLanguage != null ? selectedLanguage.toLowerCase() : "";

                        Log.d(TAG, "Found Course: " + course.getTitle() + " (LangName: " + course.getLanguageName() + ", ID: " + course.getId() + ")");
                        
                        // Flexible matching: check language name OR if title contains the target language
                        if (!target.isEmpty() && (langName.contains(target) || title.contains(target))) {
                            selectedCourseId = course.getId();
                            Log.d(TAG, "Matched Course ID: " + selectedCourseId);
                            
                            // Load Flag
                            ImageView logoIv = findViewById(R.id.topBarLogo);
                            if (logoIv != null) {
                                if (course.getFlag() != null && !course.getFlag().isEmpty() && course.getFlag().startsWith("http")) {
                                    Glide.with(HomeActivity.this)
                                         .load(course.getFlag())
                                         .placeholder(R.drawable.rualingo_logo)
                                         .transform(new CircleCrop())
                                         .into(logoIv);
                                } else {
                                    // Local fallback
                                    if ("Motu".equalsIgnoreCase(selectedLanguage)) {
                                        logoIv.setImageResource(R.drawable.central_flag);
                                    } else if ("Tok Pisin".equalsIgnoreCase(selectedLanguage)) {
                                        logoIv.setImageResource(R.drawable.png_flag);
                                    }
                                }
                            }
                            break;
                        }
                    }
                    
                    if (selectedCourseId == -1L && !response.body().isEmpty()) {
                        Log.w(TAG, "Courses found but none matched '" + selectedLanguage + "'. Picking the first course as fallback.");
                        selectedCourseId = response.body().get(0).getId();
                    }

                    fetchLessonsAndQuestions();
                } else {
                    Log.e(TAG, "Failed to fetch courses: " + response.code());
                    updateUIWithEmptyState();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                updateUIWithEmptyState();
            }
        });
    }

    private void fetchLessonsAndQuestions() {
        Log.d(TAG, "Fetching lessons for Course ID: " + selectedCourseId);
        apiService.getLessons().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Lesson>> call, @NonNull Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Lessons fetched: " + response.body().size());
                    filterLessonsByCourse(response.body());
                    Log.d(TAG, "Filtered Lessons: " + filteredLessons.size());
                    
                    apiService.getExercises().enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.d(TAG, "Questions fetched: " + response.body().size());
                                filterQuestionsByLessons(response.body());
                                Log.d(TAG, "Filtered Questions: " + allQuestions.size());
                            }
                            
                            if (filteredLessons.isEmpty()) {
                                Log.w(TAG, "No lessons found for the selected course on server.");
                                updateUIWithEmptyState();
                            } else {
                                updateUIWithRealData();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                            Log.e(TAG, "Questions API call failed", t);
                            updateUIWithEmptyState();
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to fetch lessons: " + response.code());
                    updateUIWithEmptyState();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Lesson>> call, @NonNull Throwable t) {
                Log.e(TAG, "Lessons API call failed", t);
                updateUIWithEmptyState();
            }
        });
    }

    private void filterLessonsByCourse(List<Lesson> allLessons) {
        filteredLessons.clear();
        validLessonIds.clear();
        for (Lesson lesson : allLessons) {
            if (Objects.equals(lesson.getCourseId(), selectedCourseId)) {
                filteredLessons.add(lesson);
                validLessonIds.add(lesson.getId());
            }
        }
    }

    private void filterQuestionsByLessons(List<Question> rawQuestions) {
        allQuestions.clear();
        for (Question q : rawQuestions) {
            if (q.getLessonId() != null && validLessonIds.contains(q.getLessonId())) {
                allQuestions.add(q);
            }
        }
    }

    private void updateUIWithRealData() {
        lessonContainer.removeAllViews();
        int userProgress = 3; // Unlocking first 4 for testing

        String[] unitTopics = {"Pronouns", "Greetings", "Social", "Navigation", "Numbers"};

        int size = filteredLessons.size();
        for (int index = 0; index < size; index++) {
            if (index % 5 == 0) {
                addUnitHeader((index / 5) + 1);
            }
            Lesson lesson = filteredLessons.get(index);
            
            // Override title with specific topics if they are generic
            String displayTitle = lesson.getTitle();
            if (displayTitle == null || displayTitle.toLowerCase().contains("lesson") || displayTitle.toLowerCase().contains("unit")) {
                if (index < unitTopics.length) {
                    displayTitle = unitTopics[index];
                }
            }
            
            addLessonNode(displayTitle, index + 1, index <= userProgress, lesson.getId(), null);
        }
    }

    private void updateUIWithEmptyState() {
        lessonContainer.removeAllViews();
        View emptyView = LayoutInflater.from(this).inflate(R.layout.layout_unit_header, lessonContainer, false);
        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) emptyView;
        
        TextView unitNum = emptyView.findViewById(R.id.unitNumber);
        TextView unitTitle = emptyView.findViewById(R.id.unitTitle);
        TextView unitDesc = emptyView.findViewById(R.id.unitDescription);

        unitNum.setText("NOTICE");
        unitTitle.setText("No Content Available");
        unitDesc.setText("This course is currently empty. Please check back later or add content via the Admin Console.");
        
        card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#AFAFAF")));
        lessonContainer.addView(emptyView);
    }

    private void addUnitHeader(int number) {
        View header = LayoutInflater.from(this).inflate(R.layout.layout_unit_header, lessonContainer, false);
        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) header;
        TextView unitNum = header.findViewById(R.id.unitNumber);
        TextView unitTitle = header.findViewById(R.id.unitTitle);
        TextView unitDesc = header.findViewById(R.id.unitDescription);

        unitNum.setText(String.format(java.util.Locale.getDefault(), "UNIT %d", number));
        
        // Cycle header colors (Vibrant)
        int color;
        int textColor = Color.WHITE;
        switch (number % 4) {
            case 1 -> color = Color.parseColor("#CE1126"); // Red
            case 2 -> {
                color = Color.parseColor("#FCD116"); // Yellow
                textColor = Color.BLACK;
            }
            case 3 -> color = Color.parseColor("#1A1A1A"); // Black
            default -> color = Color.parseColor("#8B4513"); // Brown
        }
        
        card.setCardBackgroundColor(ColorStateList.valueOf(color));
        unitNum.setTextColor(textColor);
        unitNum.setAlpha(0.8f);
        unitTitle.setTextColor(textColor);
        unitDesc.setTextColor(textColor);
        unitDesc.setAlpha(0.9f);
        
        // Custom titles based on unit number
        switch (number) {
            case 1 -> {
                unitTitle.setText("Core Foundations");
                unitDesc.setText("Master pronouns, greetings, and basic social skills");
            }
            case 2 -> {
                unitTitle.setText("On the Move");
                unitDesc.setText("Talk about navigation, travel, and directions");
            }
            case 3 -> {
                unitTitle.setText("Numbers & Trade");
                unitDesc.setText("Learn to count and handle money at the market");
            }
            default -> {
                unitTitle.setText("Advanced Skills");
                unitDesc.setText("Master complex sentence structures");
            }
        }
        
        lessonContainer.addView(header);
    }

    private void addLessonNode(String title, int number, boolean isUnlocked, long lessonId, List<Question> demoQuestions) {
        View node = LayoutInflater.from(this).inflate(R.layout.item_lesson_node, lessonContainer, false);
        MaterialButton btn = node.findViewById(R.id.lessonNodeButton);
        TextView titleTv = node.findViewById(R.id.lessonNodeTitle);
        TextView numberTv = node.findViewById(R.id.lessonNumberText);

        titleTv.setText(title);
        numberTv.setText(String.format(java.util.Locale.getDefault(), "%d", number));

        // Natural Zig-zag path logic (sin wave style)
        int posInUnit = (number - 1) % 5;
        float translationX = switch (posInUnit) {
            case 1 -> 160f;
            case 2 -> 240f;
            case 3 -> 120f;
            case 4 -> -120f;
            default -> 0f;
        };
        node.setTranslationX(translationX);

        if (!isUnlocked) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E5E5E5")));
            btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#AFAFAF")));
            btn.setEnabled(false);
            numberTv.setTextColor(Color.parseColor("#AFAFAF"));
        } else {
            // Color variations based on unit (Vibrant Palette)
            int unitIndex = (number - 1) / 4;
            int color;
            int darkColor;
            
            switch (unitIndex % 4) {
                case 1 -> {
                    color = ContextCompat.getColor(this, R.color.duo_red);
                    darkColor = ContextCompat.getColor(this, R.color.duo_red_dark);
                }
                case 2 -> {
                    color = ContextCompat.getColor(this, R.color.duo_black);
                    darkColor = ContextCompat.getColor(this, R.color.duo_black_dark);
                }
                case 3 -> {
                    color = ContextCompat.getColor(this, R.color.duo_yellow);
                    darkColor = ContextCompat.getColor(this, R.color.duo_yellow_dark);
                }
                default -> {
                    color = ContextCompat.getColor(this, R.color.duo_brown);
                    darkColor = ContextCompat.getColor(this, R.color.duo_brown_dark);
                }
            }
            
            btn.setBackgroundTintList(ColorStateList.valueOf(color));
            btn.setStrokeColor(ColorStateList.valueOf(darkColor));
            btn.setEnabled(true);
            numberTv.setTextColor(Color.WHITE);
        }

        btn.setOnClickListener(v -> {
            if (lessonId != -1L) {
                startLesson(lessonId);
            } else {
                Intent intent = new Intent(this, QuestionActivity.class);
                intent.putExtra("questions", (Serializable) demoQuestions);
                startActivity(intent);
            }
        });

        lessonContainer.addView(node);
    }

    private void loadOfflineDemoData() {
        allQuestions = new ArrayList<>();
    }

    private void startLesson(long lessonId) {
        List<Question> lessonQuestions = new ArrayList<>();
        for (Question q : allQuestions) {
            if (q.getLessonId() != null && q.getLessonId() == lessonId) {
                lessonQuestions.add(q);
            }
        }
        if (lessonQuestions.isEmpty()) {
            Toast.makeText(this, "No questions in this lesson.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra("questions", (Serializable) lessonQuestions);
        startActivity(intent);
    }

    private void setupBottomNav() {
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.navVocabulary).setOnClickListener(v -> startActivity(new Intent(this, VocabularyActivity.class)));

        findViewById(R.id.fabChat).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.app.rualingoapplication.database.ChatActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.topBarLogo).setOnClickListener(v -> {
            syncOnlineStatus(false);
            sessionManager.logout();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
