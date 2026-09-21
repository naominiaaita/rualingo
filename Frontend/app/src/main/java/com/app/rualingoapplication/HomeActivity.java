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
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.app.rualingoapplication.database.AppDatabase;
import com.app.rualingoapplication.database.PendingLog;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private android.widget.ProgressBar progressBar;
    private android.widget.TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        selectedLanguage = sessionManager.getSelectedLanguage();
        selectedCourseId = sessionManager.getSelectedCourseId();
        Log.d(TAG, "Selected Language: " + selectedLanguage + ", Selected Course ID: " + selectedCourseId);
        
        if ("ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);
        apiService = RetrofitClient.getApiService();
        lessonContainer = findViewById(R.id.lessonContainer);
        progressBar = findViewById(R.id.homeProgressBar);
        tvEmpty = findViewById(R.id.tvEmptyHome);

        setupBottomNav();
        setupChatButton();
        syncOnlineStatus(true);
        scheduleLogSync();
    }

    private void setupChatButton() {
        View chatBtn = findViewById(R.id.chatWithRuaContainer);
        if (chatBtn != null) {
            chatBtn.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, com.app.rualingoapplication.database.ChatActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh selected language and top bar in case user just finished a setup step
        if (sessionManager != null) {
            selectedLanguage = sessionManager.getSelectedLanguage();
            selectedCourseId = sessionManager.getSelectedCourseId();
            setupTopBar();

            // Force language selection if not already done
            if (selectedCourseId == -1L && !"ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
                Log.d(TAG, "No course selected, redirecting to LanguageSelectionActivity");
                startActivity(new Intent(this, LanguageSelectionActivity.class));
                finish();
                return;
            }
        }
        loadDataFromServer();
    }

    private void scheduleLogSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 1, java.util.concurrent.TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("LogSync", ExistingPeriodicWorkPolicy.KEEP, syncRequest);
    }

    private void syncOnlineStatus(boolean isActive) {
        if (sessionManager.getUserId() == -1) return;
        
        User update = new User();
        update.setIsActive(isActive);
        apiService.editUser(sessionManager.getUserId(), update).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Online status synced: " + isActive);
                    if (isActive) {
                        logLoginEvent();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to sync online status", t);
            }
        });
    }

    private void logLoginEvent() {
        Long userId = sessionManager.getUserId();
        String action = "USER_LOGIN";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("clientTimestamp", timestamp);
        apiService.logUserActivity(userId, payload).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.updateProfile(response.body());
                    setupTopBar(); // Refresh streak UI
                    Log.d(TAG, "Login activity logged and streak synced");
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to log login activity. Saving locally.");
                saveLogLocally(userId, action, timestamp);
            }
        });
    }

    private void saveLogLocally(Long userId, String action, long timestamp) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            db.appDao().insertPendingLog(new PendingLog(userId, action, null, null, timestamp));
            Log.d(TAG, "Login log saved locally");
        }).start();
    }

    private void setupTopBar() {
        TextView streakText = findViewById(R.id.streakCount);
        if (streakText != null) {
            streakText.setText(String.valueOf(sessionManager.getStreak()));
        }

        TextView xpText = findViewById(R.id.xpCount);
        if (xpText != null) {
            xpText.setText(String.format(java.util.Locale.getDefault(), "%d XP", sessionManager.getXP()));
        }

        TextView langNameText = findViewById(R.id.topBarLanguageName);
        View flagContainer = findViewById(R.id.topBarFlagContainer);
        
        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            if (langNameText != null) langNameText.setVisibility(View.GONE);
            if (flagContainer != null) flagContainer.setVisibility(View.GONE);
        } else {
            if (langNameText != null) {
                langNameText.setVisibility(View.VISIBLE);
                langNameText.setText(selectedLanguage);
            }
            if (flagContainer != null) flagContainer.setVisibility(View.VISIBLE);
        }
        
        ImageView logoIv = findViewById(R.id.topBarLogo);
        if (logoIv != null && selectedLanguage != null) {
            if ("Motu".equalsIgnoreCase(selectedLanguage)) {
                logoIv.setImageResource(R.drawable.central_flag);
            } else if ("Tok Pisin".equalsIgnoreCase(selectedLanguage)) {
                logoIv.setImageResource(R.drawable.png_flag);
            } else if ("Duna".equalsIgnoreCase(selectedLanguage)) {
                logoIv.setImageResource(R.drawable.hela_flag);
            } else if ("Tiang".equalsIgnoreCase(selectedLanguage)) {
                logoIv.setImageResource(R.drawable.newireland_flag);
            }
        }
    }

    private void loadDataFromServer() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading courses from server. Target language: " + selectedLanguage + " URL: " + BuildConfig.BASE_URL + "api/courses");
        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Courses response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Courses fetched: " + response.body().size());
                    
                    Course matchedCourse = null;
                    String target = selectedLanguage != null ? selectedLanguage.toLowerCase() : "";

                    // First pass: Try to find a course that matches the selected language name
                    for (Course course : response.body()) {
                        String langName = course.getLanguageName() != null ? course.getLanguageName().toLowerCase() : "";
                        String title = course.getTitle() != null ? course.getTitle().toLowerCase() : "";

                        if (!target.isEmpty() && (langName.equalsIgnoreCase(target) || langName.contains(target) || title.contains(target))) {
                            matchedCourse = course;
                            Log.d(TAG, "Found course matching language name: " + course.getTitle());
                            break;
                        }
                    }

                    // Second pass: If name match failed, try to use the selectedCourseId IF it belongs to the list
                    if (matchedCourse == null && selectedCourseId != -1L) {
                        for (Course course : response.body()) {
                            if (course.getId().equals(selectedCourseId)) {
                                matchedCourse = course;
                                break;
                            }
                        }
                    }
                    
                    if (matchedCourse != null) {
                        selectedCourseId = matchedCourse.getId();
                        sessionManager.setSelectedCourseId(selectedCourseId);
                        
                        // Sync language name if it differs slightly
                        if (matchedCourse.getLanguageName() != null && !matchedCourse.getLanguageName().equalsIgnoreCase(selectedLanguage)) {
                            sessionManager.setSelectedLanguage(matchedCourse.getLanguageName());
                            selectedLanguage = matchedCourse.getLanguageName();
                        }
                        
                        Log.d(TAG, "Successfully matched Course: " + matchedCourse.getTitle() + " (ID: " + selectedCourseId + ")");
                        updateLogo(matchedCourse);
                    } else {
                        Log.w(TAG, "No course match found for " + selectedLanguage);
                        Toast.makeText(HomeActivity.this, "Course for " + selectedLanguage + " not found. Showing default course.", Toast.LENGTH_LONG).show();
                        // If we have a cached ID, try to use it as a last resort before blind fallback
                        if (selectedCourseId == -1L && !response.body().isEmpty()) {
                            selectedCourseId = response.body().get(0).getId();
                            Log.w(TAG, "Blind fallback to first course: " + response.body().get(0).getTitle());
                        }
                    }

                    fetchLessonsAndQuestions();
                } else {
                    Log.e(TAG, "Failed to fetch courses: " + response.code());
                    Toast.makeText(HomeActivity.this, "Failed to load courses from server", Toast.LENGTH_SHORT).show();
                }
            }
            
            private void updateLogo(Course course) {
                ImageView logoIv = findViewById(R.id.topBarLogo);
                if (logoIv != null) {
                    if (course.getFlag() != null && !course.getFlag().isEmpty() && course.getFlag().startsWith("http")) {
                        Glide.with(HomeActivity.this)
                             .load(course.getFlag())
                             .placeholder(R.drawable.rualingo_logo)
                             .transform(new CircleCrop())
                             .into(logoIv);
                    } else {
                        if ("Motu".equalsIgnoreCase(selectedLanguage)) {
                            logoIv.setImageResource(R.drawable.central_flag);
                        } else if ("Tok Pisin".equalsIgnoreCase(selectedLanguage)) {
                            logoIv.setImageResource(R.drawable.png_flag);
                        } else if ("Duna".equalsIgnoreCase(selectedLanguage)) {
                            logoIv.setImageResource(R.drawable.hela_flag);
                        } else if ("Tiang".equalsIgnoreCase(selectedLanguage)) {
                            logoIv.setImageResource(R.drawable.newireland_flag);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error fetching courses", t);
                Toast.makeText(HomeActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLessonsAndQuestions() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        Log.d(TAG, "Fetching lessons for Course ID: " + selectedCourseId + " URL: " + BuildConfig.BASE_URL + "api/lessons");
        apiService.getLessons(selectedCourseId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Lesson>> call, @NonNull Response<List<Lesson>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Lessons response: " + response.code());
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    filterLessonsByCourse(response.body());

                    // Fetch completion progress to handle sequential locking
                    Log.d(TAG, "Fetching completed lessons. URL: " + BuildConfig.BASE_URL + "api/account/lessons/completed");
                    apiService.getCompletedLessons().enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> responseProgress) {
                            Log.d(TAG, "Progress response: " + responseProgress.code());
                            final List<Long> completedIds = new ArrayList<>();
                            if (responseProgress.isSuccessful() && responseProgress.body() != null) {
                                for (Map<String, Object> map : responseProgress.body()) {
                                    Object id = map.get("lessonId");
                                    if (id == null) id = map.get("lesson_id");
                                    if (id instanceof Number) completedIds.add(((Number) id).longValue());
                                }
                            }

                            Log.d(TAG, "Fetching exercises. URL: " + BuildConfig.BASE_URL + "api/exercises");
                            apiService.getExercises(null).enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> responseEx) {
                                    Log.d(TAG, "Exercises response: " + responseEx.code());
                                    if (responseEx.isSuccessful() && responseEx.body() != null) {
                                        filterQuestionsByLessons(responseEx.body());
                                    }
                                    
                                    if (filteredLessons.isEmpty()) {
                                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                                        Toast.makeText(HomeActivity.this, R.string.no_lessons_found, Toast.LENGTH_SHORT).show();
                                        lessonContainer.removeAllViews();
                                    } else {
                                        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                                        updateUIWithRealProgress(completedIds);
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                                    Log.e(TAG, "Error fetching exercises", t);
                                    updateUIWithRealProgress(completedIds);
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                            Log.e(TAG, "Error fetching progress", t);
                            updateUIWithRealProgress(new ArrayList<>());
                        }
                    });
                } else {
                    if (tvEmpty != null && filteredLessons.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                    Log.w(TAG, "No lessons found or error: " + response.code());
                    Toast.makeText(HomeActivity.this, "No lessons available", Toast.LENGTH_SHORT).show();
                    lessonContainer.removeAllViews();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Lesson>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvEmpty != null && filteredLessons.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error fetching lessons", t);
                Toast.makeText(HomeActivity.this, "Failed to load lessons", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMockData() {
        Log.w(TAG, "loadMockData called but seeding is disabled. Clearing view.");
        lessonContainer.removeAllViews();
    }

    private List<Question> createMockQuestions(String topic) {
        return new ArrayList<>();
    }

    private void filterLessonsByCourse(List<Lesson> allLessons) {
        Log.d(TAG, "Filtering " + allLessons.size() + " lessons for Course ID: " + selectedCourseId);
        filteredLessons.clear();
        validLessonIds.clear();
        for (Lesson lesson : allLessons) {
            Long lCourseId = lesson.getCourseId();
            if (lCourseId != null && lCourseId.equals(selectedCourseId)) {
                filteredLessons.add(lesson);
                validLessonIds.add(lesson.getId());
            }
        }
        Log.d(TAG, "Filtered lessons count: " + filteredLessons.size() + " for Course ID: " + selectedCourseId);
        // Force Fix: Sort lessons by ID to ensure sequential progression works (L1, L2, L3...)
        java.util.Collections.sort(filteredLessons, (a, b) -> Long.compare(a.getId(), b.getId()));
    }

    private void filterQuestionsByLessons(List<Question> rawQuestions) {
        allQuestions.clear();
        for (Question q : rawQuestions) {
            if (q.getLessonId() != null && validLessonIds.contains(q.getLessonId())) {
                allQuestions.add(q);
            }
        }
    }

    private void updateUIWithRealProgress(List<Long> completedIds) {
        Log.d(TAG, "Updating UI with Real Progress. Completed Lesson IDs: " + completedIds);
        lessonContainer.removeAllViews();
        String[] unitTopics = {"Greetings", "Pronouns", "Social", "Navigation", "Numbers"};

        int size = filteredLessons.size();
        boolean previousLessonCompleted = true; // First lesson is always unlocked

        for (int index = 0; index < size; index++) {
            if (index % 5 == 0) {
                addUnitHeader((index / 5) + 1);
            }
            Lesson lesson = filteredLessons.get(index);
            
            String displayTitle = lesson.getTitle();
            if (displayTitle == null || displayTitle.toLowerCase().contains("lesson") || displayTitle.toLowerCase().contains("unit")) {
                if (index < unitTopics.length) {
                    displayTitle = unitTopics[index];
                }
            }
            
            // Force Fix: A lesson is unlocked if it's the first one OR if the previous one is in the completed list.
            boolean isUnlocked = (index == 0) || previousLessonCompleted;
            Log.d(TAG, "Rendering Lesson: " + lesson.getTitle() + " (ID: " + lesson.getId() + "), Unlocked: " + isUnlocked);

            addLessonNode(displayTitle, index + 1, isUnlocked, lesson.getId(), null);
            
            // Update completion status for the NEXT lesson
            previousLessonCompleted = completedIds.contains(lesson.getId());
        }
    }

    private void addUnitHeader(int number) {
        View header = LayoutInflater.from(this).inflate(R.layout.layout_unit_header, lessonContainer, false);
        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) header;
        TextView unitNum = header.findViewById(R.id.unitNumber);
        TextView unitTitle = header.findViewById(R.id.unitTitle);
        TextView unitDesc = header.findViewById(R.id.unitDescription);

        unitNum.setText(String.format(java.util.Locale.getDefault(), "UNIT %d", number));
        
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
        if (unitNum != null) {
            unitNum.setTextColor(textColor);
            unitNum.setAlpha(0.8f);
            unitNum.setVisibility(View.GONE);
        }
        unitTitle.setTextColor(textColor);
        unitDesc.setTextColor(textColor);
        unitDesc.setAlpha(0.9f);
        
        String titlePrefix = String.format(java.util.Locale.getDefault(), "UNIT %d: ", number);
        switch (number) {
            case 1 -> {
                unitTitle.setText(titlePrefix + "Core Foundations");
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
        btn.setText(String.format(java.util.Locale.getDefault(), "LESSON %d", number));

        // Staggered zig-zag snake pattern to precisely replicate Duolingo's path as shown in the screenshot
        int posInUnit = (number - 1) % 5;
        float translationX = switch (posInUnit) {
            case 0 -> -120f; // Center-left
            case 1 -> 80f;   // Center-right
            case 2 -> 240f;  // Far-right
            case 3 -> 60f;   // Shifting back left
            case 4 -> -100f; // Far-left/center-left
            default -> 0f;
        };
        node.setTranslationX(translationX);

        int unitNumber = (number - 1) / 5 + 1;

        if (!isUnlocked) {
            btn.setBackgroundResource(R.drawable.btn_3d_gray_selectable);
            btn.setEnabled(false);
            numberTv.setTextColor(Color.parseColor("#AFAFAF"));
        } else {
            btn.setEnabled(true);
            numberTv.setTextColor(Color.WHITE);
            btn.setBackgroundTintList(null); // Clear any flat tint to allow full 3D drawable layers rendering
            
            // Match the lesson node 3D background with the corresponding unit's theme color
            switch (unitNumber % 4) {
                case 1 -> btn.setBackgroundResource(R.drawable.btn_3d_red);
                case 2 -> btn.setBackgroundResource(R.drawable.btn_3d_gold);
                case 3 -> btn.setBackgroundResource(R.drawable.btn_3d_png_black);
                default -> btn.setBackgroundResource(R.drawable.btn_3d_blue);
            }
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

    private void startLesson(long lessonId) {
        sessionManager.setCurrentLessonId(lessonId);
        List<Question> lessonQuestions = new ArrayList<>();
        for (Question q : allQuestions) {
            if (q.getLessonId() != null && q.getLessonId() == lessonId) {
                lessonQuestions.add(q);
            }
        }

        apiService.getVocabulary(selectedCourseId, null, lessonId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<VocabularyItem>> call, @NonNull Response<List<VocabularyItem>> response) {
                List<Question> fullList = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (VocabularyItem v : response.body()) {
                        Question flashcard = new Question();
                        flashcard.setType("vocabulary");
                        flashcard.setQuestion(v.getWordTarget()); 
                        flashcard.setAnswer(v.getTranslation()); 
                        flashcard.setQuestionText(v.getWord());
                        flashcard.setPhonetic(v.getPhonetic());
                        flashcard.setExampleSentence(v.getExampleSentence());
                        flashcard.setTopic(v.getTopic());
                        flashcard.setAudioPath(v.getAudioPath());
                        flashcard.setLessonId(lessonId);
                        fullList.add(flashcard);
                    }
                }
                fullList.addAll(lessonQuestions);

                if (fullList.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "No content in this lesson.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Intent intent = new Intent(HomeActivity.this, QuestionActivity.class);
                intent.putExtra("questions", (Serializable) fullList);
                startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Call<List<VocabularyItem>> call, @NonNull Throwable t) {
                if (lessonQuestions.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "No questions in this lesson.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, QuestionActivity.class);
                intent.putExtra("questions", (Serializable) lessonQuestions);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNav() {
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.navLanguages).setOnClickListener(v -> startActivity(new Intent(this, LanguageSelectionActivity.class)));
        findViewById(R.id.navVocabulary).setOnClickListener(v -> startActivity(new Intent(this, VocabularyActivity.class)));
    }
}
