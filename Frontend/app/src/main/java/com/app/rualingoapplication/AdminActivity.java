package com.app.rualingoapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private TextView tvHeaderTitle, tvTotalLearners, tvSyncQueue, tvAdminUsername, tvAdminRole;
    private TextView tvEmptyLanguages, tvEmptyCourses, tvEmptyLessons, tvEmptyExercises, tvEmptyVocab;
    private android.widget.ProgressBar adminProgressBar;
    private View btnLogout;
    private View overviewSection, languagesSection, coursesSection, exercisesSection, vocabularySection;
    private View coursesSubSection, lessonsSubSection;
    private BottomNavigationView bottomNavigation;
    private EditText etSearchUsers;
    private RecyclerView rvUserRoster;
    
    // Form fields
    private TextInputEditText languageNameET, provinceET, districtET, clanET, flagET, sourceET;
    private TextInputEditText courseTitleET, courseDescET, courseModerationNoteET, courseReviewedAtET, courseStatusET, courseFlagET;
    private TextInputEditText lessonTitleET, lessonDescriptionET, lessonContentET, lessonModerationNoteET, lessonReviewedAtET, lessonStatusET, lessonTopicET;
    private TextInputEditText exercisePromptET, exerciseQuestionTextET, exerciseAnsET, exerciseOptionsET, exerciseHintET;
    private TextInputEditText exercisePhoneticET, exerciseExampleET, exerciseTopicET, exerciseAudioET, exerciseSubTypeET, exerciseMetadataET;
    private TextInputEditText vocabWordET, vocabTargetET, vocabPhoneticET, vocabTranslationET, vocabExampleET, vocabTopicET, vocabAudioPathET;
    
    // Spinners
    private MaterialAutoCompleteTextView languageSpinnerForCourses, courseSpinnerForLessons, lessonSpinnerForExercises, exerciseTypeSpinner;
    private MaterialAutoCompleteTextView vocabLessonSpinner;
    
    private ApiService apiService;
    private SessionManager sessionManager;
    
    private final List<LanguageModel> languagesList = new ArrayList<>();
    private final List<Course> coursesList = new ArrayList<>();
    private final List<Lesson> lessonsList = new ArrayList<>();
    private final List<Question> exercisesList = new ArrayList<>();
    private final List<VocabularyItem> vocabularyList = new ArrayList<>();
    private LanguageDetailAdapter languagesAdapter;
    private VocabularyManageAdapter vocabAdapter;
    
    private Long selectedLanguageId = null;
    private Long selectedCourseId = null;
    private Long selectedLessonId = null;
    private Long selectedExerciseId = null;
    private Long selectedVocabId = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        if (!"ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);
        apiService = RetrofitClient.getApiService();

        bindViews();
        setupBottomNav();
        setupLogout();
        setupCrudActions();
        setupDatePickers();
        setupCascadingSpinners();
        
        loadDashboardStats();
        loadInitialData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileInfo();
    }

    private void refreshProfileInfo() {
        if (sessionManager != null && tvAdminUsername != null) {
            String fullName = sessionManager.getFirstName() + " " + sessionManager.getSecondName();
            tvAdminUsername.setText(fullName.trim().isEmpty() ? sessionManager.getUsername() : fullName);
            tvAdminRole.setText("Administrator • " + (sessionManager.getProvinceOfOrigin() != null ? sessionManager.getProvinceOfOrigin() : "System"));
        }
    }

    private void bindViews() {
        // Sections
        overviewSection = findViewById(R.id.overviewSection);
        languagesSection = findViewById(R.id.languagesSection);
        coursesSection = findViewById(R.id.coursesSection);
        exercisesSection = findViewById(R.id.exercisesSection);
        vocabularySection = findViewById(R.id.vocabularySection);

        // Courses & Lessons Sub-tabs
        com.google.android.material.tabs.TabLayout coursesSubTabLayout = findViewById(R.id.coursesSubTabLayout);
        coursesSubSection = findViewById(R.id.coursesSubSection);
        lessonsSubSection = findViewById(R.id.lessonsSubSection);

        if (coursesSubTabLayout != null) {
            coursesSubTabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0 -> {
                            coursesSubSection.setVisibility(View.VISIBLE);
                            lessonsSubSection.setVisibility(View.GONE);
                        }
                        case 1 -> {
                            coursesSubSection.setVisibility(View.GONE);
                            lessonsSubSection.setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
                @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
        }

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnLogout = findViewById(R.id.btnLogout);
        tvTotalLearners = findViewById(R.id.tvTotalLearners);
        tvSyncQueue = findViewById(R.id.tvSyncQueue);
        tvAdminUsername = findViewById(R.id.tvAdminUsername);
        tvAdminRole = findViewById(R.id.tvAdminRole);
        etSearchUsers = findViewById(R.id.etSearchUsers);
        rvUserRoster = findViewById(R.id.rvUserRoster);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        adminProgressBar = findViewById(R.id.adminProgressBar);

        tvEmptyLanguages = findViewById(R.id.tvEmptyLanguages);
        tvEmptyCourses = findViewById(R.id.tvEmptyCourses);
        tvEmptyLessons = findViewById(R.id.tvEmptyLessons);
        tvEmptyExercises = findViewById(R.id.tvEmptyExercises);
        tvEmptyVocab = findViewById(R.id.tvEmptyVocab);

        View profileCard = findViewById(R.id.cardCurrentAdmin);
        if (profileCard != null) {
            profileCard.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        if (rvUserRoster != null) {
            rvUserRoster.setLayoutManager(new LinearLayoutManager(this));
        }

        // Forms
        languageNameET = findViewById(R.id.languageNameEditText);
        provinceET = findViewById(R.id.provinceEditText);
        districtET = findViewById(R.id.districtEditText);
        clanET = findViewById(R.id.clanEditText);
        flagET = findViewById(R.id.flagEditText);
        sourceET = findViewById(R.id.sourceEditText);
        
        courseTitleET = findViewById(R.id.courseTitleEditText);
        courseDescET = findViewById(R.id.courseDescriptionInput);
        courseModerationNoteET = findViewById(R.id.courseModerationNoteInput);
        courseStatusET = findViewById(R.id.courseStatusInput);
        courseFlagET = findViewById(R.id.courseFlagInput);
        courseReviewedAtET = findViewById(R.id.courseReviewedAtInput);
        
        lessonTitleET = findViewById(R.id.lessonTitleEditText);
        lessonDescriptionET = findViewById(R.id.lessonDescriptionEditText);
        lessonContentET = findViewById(R.id.lessonDescEditText);
        lessonTopicET = findViewById(R.id.lessonTopicEditText);
        lessonModerationNoteET = findViewById(R.id.lessonModerationNoteEditText);
        lessonReviewedAtET = findViewById(R.id.lessonReviewedAtEditText);
        lessonStatusET = findViewById(R.id.lessonStatusEditText);

        exercisePromptET = findViewById(R.id.exerciseQuestionEditText);
        exerciseQuestionTextET = findViewById(R.id.exerciseQuestionTextEditText);
        exerciseAnsET = findViewById(R.id.correctAnsEditText);
        exerciseOptionsET = findViewById(R.id.optionsEditText);
        exerciseHintET = findViewById(R.id.exerciseHintEditText);
        exercisePhoneticET = findViewById(R.id.exercisePhoneticEditText);
        exerciseExampleET = findViewById(R.id.exerciseExampleEditText);
        exerciseTopicET = findViewById(R.id.exerciseTopicEditText);
        exerciseAudioET = findViewById(R.id.exerciseAudioEditText);
        exerciseSubTypeET = findViewById(R.id.exerciseSubTypeEditText);
        exerciseMetadataET = findViewById(R.id.exerciseMetadataEditText);

        vocabWordET = findViewById(R.id.vocabWordEditText);
        vocabTargetET = findViewById(R.id.vocabTargetEditText);
        vocabPhoneticET = findViewById(R.id.vocabPhoneticEditText);
        vocabTranslationET = findViewById(R.id.vocabTranslationEditText);
        vocabExampleET = findViewById(R.id.vocabExampleEditText);
        vocabTopicET = findViewById(R.id.vocabTopicEditText);
        vocabAudioPathET = findViewById(R.id.vocabAudioPathEditText);

        languageSpinnerForCourses = findViewById(R.id.languageSpinnerForCourses);
        courseSpinnerForLessons = findViewById(R.id.courseSpinnerForLessons);
        lessonSpinnerForExercises = findViewById(R.id.lessonSpinnerForExercises);
        exerciseTypeSpinner = findViewById(R.id.exerciseTypeSpinner);
        vocabLessonSpinner = findViewById(R.id.vocabLessonSpinner);

        RecyclerView langRV = findViewById(R.id.languagesRecyclerView);
        if (langRV != null) {
            langRV.setLayoutManager(new LinearLayoutManager(this));
            languagesAdapter = new LanguageDetailAdapter(languagesList, this::populateLanguageFields);
            langRV.setAdapter(languagesAdapter);
        }

        RecyclerView coursesRV = findViewById(R.id.coursesRecyclerView);
        if (coursesRV != null) coursesRV.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView lessonsRV = findViewById(R.id.lessonsRecyclerView);
        if (lessonsRV != null) lessonsRV.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView exercisesRV = findViewById(R.id.exercisesRecyclerView);
        if (exercisesRV != null) exercisesRV.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView vocabRV = findViewById(R.id.vocabRecyclerView);
        if (vocabRV != null) {
            vocabRV.setLayoutManager(new LinearLayoutManager(this));
            vocabAdapter = new VocabularyManageAdapter(vocabularyList, languagesList, coursesList, lessonsList, new VocabularyManageAdapter.OnVocabActionListener() {
                @Override public void onEdit(VocabularyItem item) { populateVocabFields(item); }
                @Override public void onDelete(VocabularyItem item) { deleteVocab(item); }
            });
            vocabRV.setAdapter(vocabAdapter);
        }
    }

    private void setupBottomNav() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.admin_overview) switchSection("overview");
                else if (id == R.id.admin_languages) switchSection("languages");
                else if (id == R.id.admin_lessons) switchSection("courses");
                else if (id == R.id.admin_exercises) switchSection("exercises");
                else if (id == R.id.admin_vocabulary) switchSection("vocabulary");
                return true;
            });
        }
    }

    private void switchSection(String section) {
        if (overviewSection != null) overviewSection.setVisibility(View.GONE);
        if (languagesSection != null) languagesSection.setVisibility(View.GONE);
        if (coursesSection != null) coursesSection.setVisibility(View.GONE);
        if (exercisesSection != null) exercisesSection.setVisibility(View.GONE);
        if (vocabularySection != null) vocabularySection.setVisibility(View.GONE);

        switch (section) {
            case "overview" -> {
                if (overviewSection != null) overviewSection.setVisibility(View.VISIBLE);
                if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.admin_center);
            }
            case "languages" -> {
                if (languagesSection != null) languagesSection.setVisibility(View.VISIBLE);
                if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.languages);
            }
            case "courses" -> {
                if (coursesSection != null) coursesSection.setVisibility(View.VISIBLE);
                if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.courses_lessons);
            }
            case "exercises" -> {
                if (exercisesSection != null) exercisesSection.setVisibility(View.VISIBLE);
                if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.exercises);
            }
            case "vocabulary" -> {
                if (vocabularySection != null) vocabularySection.setVisibility(View.VISIBLE);
                if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.vocabulary_tab);
            }
        }
    }

    private void setupLogout() {
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.logout();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupCrudActions() {
        View createLangBtn = findViewById(R.id.createLanguageButton);
        if (createLangBtn != null) createLangBtn.setOnClickListener(v -> createLanguage());
        View updateLangBtn = findViewById(R.id.updateLanguageButton);
        if (updateLangBtn != null) updateLangBtn.setOnClickListener(v -> updateLanguage());
        View deleteLangBtn = findViewById(R.id.deleteLanguageButton);
        if (deleteLangBtn != null) deleteLangBtn.setOnClickListener(v -> deleteLanguage());

        View createCourseBtn = findViewById(R.id.createCourseButton);
        if (createCourseBtn != null) createCourseBtn.setOnClickListener(v -> createCourse());
        View updateCourseBtn = findViewById(R.id.updateCourseButton);
        if (updateCourseBtn != null) updateCourseBtn.setOnClickListener(v -> updateCourse());
        View deleteCourseBtn = findViewById(R.id.deleteCourseButton);
        if (deleteCourseBtn != null) deleteCourseBtn.setOnClickListener(v -> deleteCourse());

        View createLessonBtn = findViewById(R.id.createLessonButton);
        if (createLessonBtn != null) createLessonBtn.setOnClickListener(v -> createLesson());
        View updateLessonBtn = findViewById(R.id.updateLessonButton);
        if (updateLessonBtn != null) updateLessonBtn.setOnClickListener(v -> updateLesson());

        View createExBtn = findViewById(R.id.createExerciseButton);
        if (createExBtn != null) createExBtn.setOnClickListener(v -> createExercise());
        View updateExBtn = findViewById(R.id.updateExerciseButton);
        if (updateExBtn != null) updateExBtn.setOnClickListener(v -> updateExercise());

        View createVocabBtn = findViewById(R.id.createVocabButton);
        if (createVocabBtn != null) createVocabBtn.setOnClickListener(v -> createVocab());
        View updateVocabBtn = findViewById(R.id.updateVocabButton);
        if (updateVocabBtn != null) updateVocabBtn.setOnClickListener(v -> updateVocab());

        setupDatePickers();
    }

    private void setupCascadingSpinners() {
        if (courseSpinnerForLessons != null) {
            courseSpinnerForLessons.setOnItemClickListener((parent, view, position, id) -> {
                String selectedCourse = (String) parent.getItemAtPosition(position);
                updateLessonSpinnerForExercises(selectedCourse);
                updateLessonSpinnerForVocab(selectedCourse);
            });
        }
    }

    private void updateCourseSpinnerForVocab(String langName) {
    }

    private void updateLessonSpinnerForVocab(String courseTitle) {
        Long courseId = null;
        for (Course c : coursesList) {
            if (courseTitle.equals(c.getTitle())) {
                courseId = c.getId();
                break;
            }
        }
        if (courseId == null) return;
        List<String> titles = new ArrayList<>();
        for (Lesson l : lessonsList) {
            if (Objects.equals(l.getCourseId(), courseId)) titles.add(l.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, titles);
        if (vocabLessonSpinner != null) vocabLessonSpinner.setAdapter(adapter);
    }

    private void updateCourseSpinnerForExercises(String langName) {
    }

    private void updateLessonSpinnerForExercises(String courseTitle) {
        Long courseId = null;
        for (Course c : coursesList) {
            if (courseTitle.equals(c.getTitle())) {
                courseId = c.getId();
                break;
            }
        }
        if (courseId == null) return;
        List<String> titles = new ArrayList<>();
        for (Lesson l : lessonsList) {
            if (Objects.equals(l.getCourseId(), courseId)) titles.add(l.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, titles);
        if (lessonSpinnerForExercises != null) lessonSpinnerForExercises.setAdapter(adapter);
    }

    private void setupDatePickers() {
        if (courseReviewedAtET != null) courseReviewedAtET.setOnClickListener(v -> showDatePicker(courseReviewedAtET));
        if (lessonReviewedAtET != null) lessonReviewedAtET.setOnClickListener(v -> showDatePicker(lessonReviewedAtET));
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editText.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void populateLanguageFields(LanguageModel lang) {
        selectedLanguageId = lang.getId();
        languageNameET.setText(lang.getName() != null ? lang.getName() : lang.getLanguageName());
        provinceET.setText(lang.getProvince());
        districtET.setText(lang.getDistrict());
        clanET.setText(lang.getClan());
        flagET.setText(lang.getFlag());
        sourceET.setText(lang.getSource());
        Toast.makeText(this, "Editing: " + (lang.getName() != null ? lang.getName() : lang.getLanguageName()), Toast.LENGTH_SHORT).show();
    }

    private void populateCourseFields(Course course) {
        selectedCourseId = course.getId();
        courseTitleET.setText(course.getTitle());
        courseDescET.setText(course.getDescription());
        courseModerationNoteET.setText(course.getModerationNote());
        courseReviewedAtET.setText(course.getReviewedAt());
        courseStatusET.setText(course.getSubmissionStatus());
        courseFlagET.setText(course.getFlag());
        
        // Find and set language in spinner
        if (languageSpinnerForCourses != null) {
            for (LanguageModel l : languagesList) {
                if (Objects.equals(l.getId(), course.getLanguageId())) {
                    languageSpinnerForCourses.setText(l.getName() != null ? l.getName() : l.getLanguageName(), false);
                    break;
                }
            }
        }
        Toast.makeText(this, getString(R.string.found_course_format, course.getTitle()), Toast.LENGTH_SHORT).show();
    }

    private void populateLessonFields(Lesson lesson) {
        selectedLessonId = lesson.getId();
        lessonTitleET.setText(lesson.getTitle());
        lessonDescriptionET.setText(lesson.getDescription());
        lessonContentET.setText(lesson.getContent());
        lessonModerationNoteET.setText(lesson.getModerationNote());
        lessonReviewedAtET.setText(lesson.getReviewedAt());
        lessonStatusET.setText(lesson.getSubmissionStatus());
        lessonTopicET.setText(lesson.getTopic());

        if (courseSpinnerForLessons != null) {
            for (Course c : coursesList) {
                if (Objects.equals(c.getId(), lesson.getCourseId())) {
                    courseSpinnerForLessons.setText(c.getTitle(), false);
                    break;
                }
            }
        }
        Toast.makeText(this, getString(R.string.found_lesson_format, lesson.getTitle()), Toast.LENGTH_SHORT).show();
    }

    private void loadDashboardStats() {
        showLoading(true);
        Log.d("AdminActivity", "Loading courses for dashboard. URL: " + BuildConfig.BASE_URL + "api/courses");
        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                showLoading(false);
                Log.d("AdminActivity", "Courses dashboard response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    updateCourseList(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("AdminActivity", "Failed to load courses for dashboard", t);
            }
        });

        Log.d("AdminActivity", "Fetching users. URL: " + BuildConfig.BASE_URL + "api/users");
        apiService.fetchAllUsers().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    calculatePlatformMetrics(response.body());
                    updateModerationLists(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Fetch users failed", t);
            }
        });
    }

    private void calculatePlatformMetrics(List<User> users) {
        if (users == null || users.isEmpty()) return;
        int activeCount = 0;
        for (User u : users) {
            Integer roleId = u.getRoleId();
            String roleStr = u.getRole();
            boolean isAdmin = (roleId != null && roleId == 1) || ("1".equals(roleStr)) || (roleStr != null && roleStr.toUpperCase().contains("ADMIN"));
            if (!isAdmin) activeCount++;
        }
        if (tvTotalLearners != null) tvTotalLearners.setText(String.valueOf(activeCount));
        if (tvSyncQueue != null) tvSyncQueue.setText("0 Pending");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadInitialData() {
        showLoading(true);
        Log.d("AdminActivity", "Loading languages. URL: " + BuildConfig.BASE_URL + "api/languages");
        apiService.getLanguages().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<LanguageModel>> call, @NonNull Response<List<LanguageModel>> response) {
                showLoading(false);
                Log.d("AdminActivity", "Languages response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    languagesList.clear();
                    languagesList.addAll(response.body());
                    updateLanguageSpinner();
                    updateLanguageList(languagesList);
                    if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
                    refreshLanguageStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<LanguageModel>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("AdminActivity", "Languages load failed", t);
            }
        });

        Log.d("AdminActivity", "Loading lessons. URL: " + BuildConfig.BASE_URL + "api/lessons");
        apiService.getLessons(null).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Lesson>> call, @NonNull Response<List<Lesson>> response) {
                Log.d("AdminActivity", "Lessons response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    updateLessonList(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Lesson>> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Lessons load failed", t);
            }
        });

        Log.d("AdminActivity", "Loading exercises. URL: " + BuildConfig.BASE_URL + "api/exercises");
        apiService.getExercises(null).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                Log.d("AdminActivity", "Exercises response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    updateExerciseList(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Exercises load failed", t);
            }
        });

        loadVocabulary();
        updateExerciseTypeSpinner();
    }

    private void showLoading(boolean loading) {
        if (adminProgressBar != null) {
            adminProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void loadVocabulary() {
        showLoading(true);
        Log.d("AdminActivity", "Loading vocabulary. URL: " + BuildConfig.BASE_URL + "api/vocabulary");
        apiService.getVocabulary(null, null, null).enqueue(new Callback<List<VocabularyItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<VocabularyItem>> call, @NonNull Response<List<VocabularyItem>> response) {
                showLoading(false);
                Log.d("AdminActivity", "Vocabulary response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    vocabularyList.clear();
                    vocabularyList.addAll(response.body());
                    if (tvEmptyVocab != null) {
                        tvEmptyVocab.setVisibility(vocabularyList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
                    refreshLanguageStats();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VocabularyItem>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("AdminActivity", "Vocab load failed", t);
            }
        });
    }

    private void refreshLanguageStats() {
        for (LanguageModel lang : languagesList) {
            Long langId = lang.getId();
            int cCount = 0, lCount = 0, eCount = 0, vCount = 0;
            
            for (Course c : coursesList) {
                if (Objects.equals(langId, c.getLanguageId())) {
                    cCount++;
                    for (Lesson l : lessonsList) {
                        if (Objects.equals(c.getId(), l.getCourseId())) {
                            lCount++;
                            for (Question e : exercisesList) {
                                if (Objects.equals(l.getId(), e.getLessonId())) {
                                    eCount++;
                                }
                            }
                        }
                    }
                }
            }
            
            for (VocabularyItem v : vocabularyList) {
                // Vocabulary is now only linked to Lesson
                // Finding the language context requires traversing Lesson -> Course -> Language
            }
            
            lang.setCourseCount(cCount);
            lang.setLessonCount(lCount);
            lang.setExerciseCount(eCount);
            lang.setVocabCount(vCount);
        }

        if (languagesAdapter != null) {
            languagesAdapter.notifyDataSetChanged();
        }
    }

    private void updateExerciseTypeSpinner() {
        String[] types = {"multiple_choice", "fill_blank", "short_answer", "matching", "true_false"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, types);
        if (exerciseTypeSpinner != null) exerciseTypeSpinner.setAdapter(adapter);
    }

    private void updateLanguageSpinner() {
        List<String> names = new ArrayList<>();
        for (LanguageModel l : languagesList) {
            String displayName = l.getName() != null ? l.getName() : l.getLanguageName();
            if (displayName != null) names.add(displayName);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, names);
        if (languageSpinnerForCourses != null) languageSpinnerForCourses.setAdapter(adapter);
    }

    private void updateCourseSpinner() {
        List<String> titles = new ArrayList<>();
        for (Course c : coursesList) titles.add(c.getTitle());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, titles);
        if (courseSpinnerForLessons != null) courseSpinnerForLessons.setAdapter(adapter);
    }

    private void updateLessonSpinner() {
        List<String> titles = new ArrayList<>();
        for (Lesson l : lessonsList) titles.add(l.getTitle());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, titles);
        if (lessonSpinnerForExercises != null) lessonSpinnerForExercises.setAdapter(adapter);
        if (vocabLessonSpinner != null) vocabLessonSpinner.setAdapter(adapter);
    }

    private void updateModerationLists(List<User> allUsers) {
        UserModerationAdapter rosterAdapter = new UserModerationAdapter(allUsers, new UserModerationAdapter.OnUserActionListener() {
            @Override public void onDeleteClick(User user) { deleteUser(user); }
            @Override public void onPromoteClick(User user) { promoteUser(user); }
            @Override public void onHistoryClick(User user) { viewUserHistory(user); }
        });
        if (rvUserRoster != null) rvUserRoster.setAdapter(rosterAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateLanguageList(List<LanguageModel> languages) {
        if (languages != this.languagesList) {
            this.languagesList.clear();
            this.languagesList.addAll(languages);
        }
        if (tvEmptyLanguages != null) {
            tvEmptyLanguages.setVisibility(this.languagesList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (languagesAdapter != null) {
            languagesAdapter.notifyDataSetChanged();
        }
        refreshLanguageStats();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCourseList(List<Course> courses) {
        if (courses != this.coursesList) {
            this.coursesList.clear();
            this.coursesList.addAll(courses);
        }
        if (tvEmptyCourses != null) {
            tvEmptyCourses.setVisibility(this.coursesList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        updateCourseSpinner();
        CourseDetailAdapter adapter = new CourseDetailAdapter(this.coursesList, this::populateCourseFields);
        RecyclerView coursesRV = findViewById(R.id.coursesRecyclerView);
        if (coursesRV != null) coursesRV.setAdapter(adapter);
        if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
        refreshLanguageStats();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateLessonList(List<Lesson> lessons) {
        if (lessons != this.lessonsList) {
            this.lessonsList.clear();
            this.lessonsList.addAll(lessons);
        }
        if (tvEmptyLessons != null) {
            tvEmptyLessons.setVisibility(this.lessonsList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        updateLessonSpinner();
        LessonManageAdapter adapter = new LessonManageAdapter(this.lessonsList, new LessonManageAdapter.OnLessonActionListener() {
            @Override public void onEdit(Lesson item) { populateLessonFields(item); }
            @Override public void onDelete(Lesson item) { deleteLesson(item); }
        });
        RecyclerView lessonsRV = findViewById(R.id.lessonsRecyclerView);
        if (lessonsRV != null) lessonsRV.setAdapter(adapter);
        refreshLanguageStats();
    }

    private void updateExerciseList(List<Question> exercises) {
        if (exercises != this.exercisesList) {
            this.exercisesList.clear();
            this.exercisesList.addAll(exercises);
        }
        if (tvEmptyExercises != null) {
            tvEmptyExercises.setVisibility(this.exercisesList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        QuestionManageAdapter adapter = new QuestionManageAdapter(this.exercisesList, new QuestionManageAdapter.OnQuestionActionListener() {
            @Override public void onEdit(Question item) { populateExerciseFields(item); }
            @Override public void onDelete(Question item) { deleteExercise(item); }
        });
        RecyclerView exercisesRV = findViewById(R.id.exercisesRecyclerView);
        if (exercisesRV != null) exercisesRV.setAdapter(adapter);
    }

    private void createLanguage() {
        String name = languageNameET.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Language name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        LanguageModel lang = new LanguageModel();
        lang.setName(name);
        lang.setLanguageName(name);
        lang.setProvince(provinceET.getText().toString().trim());
        lang.setDistrict(districtET.getText().toString().trim());
        lang.setClan(clanET.getText().toString().trim());
        lang.setFlag(flagET.getText().toString().trim());
        lang.setSource(sourceET.getText().toString().trim());

        Log.d("AdminActivity", "Creating language at: " + BuildConfig.BASE_URL + "api/languages");
        apiService.createLanguage(lang).enqueue(new Callback<LanguageModel>() {
            @Override
            public void onResponse(@NonNull Call<LanguageModel> call, @NonNull Response<LanguageModel> response) {
                Log.d("AdminActivity", "Create language response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.language_created, Toast.LENGTH_SHORT).show();
                    clearLanguageFields();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<LanguageModel> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Create language failed", t);
                Toast.makeText(AdminActivity.this, "Error creating language", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearLanguageFields() {
        selectedLanguageId = null;
        languageNameET.setText("");
        provinceET.setText("");
        districtET.setText("");
        clanET.setText("");
        flagET.setText("");
        sourceET.setText("");
    }

    private void updateLanguage() {
        if (selectedLanguageId == null) {
            Toast.makeText(this, "Select a language to update", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = languageNameET.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Language name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        LanguageModel lang = new LanguageModel();
        lang.setId(selectedLanguageId);
        lang.setName(name);
        lang.setLanguageName(name);
        lang.setProvince(provinceET.getText().toString().trim());
        lang.setDistrict(districtET.getText().toString().trim());
        lang.setClan(clanET.getText().toString().trim());
        lang.setFlag(flagET.getText().toString().trim());
        lang.setSource(sourceET.getText().toString().trim());

        Log.d("AdminActivity", "Updating language at: " + BuildConfig.BASE_URL + "api/languages/" + selectedLanguageId);
        apiService.updateLanguage(selectedLanguageId, lang).enqueue(new Callback<LanguageModel>() {
            @Override
            public void onResponse(@NonNull Call<LanguageModel> call, @NonNull Response<LanguageModel> response) {
                Log.d("AdminActivity", "Update language response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.language_updated, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<LanguageModel> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Update language failed", t);
            }
        });
    }

    private void deleteLanguage() {
        if (selectedLanguageId == null) return;
        Log.d("AdminActivity", "Deleting language at: " + BuildConfig.BASE_URL + "api/languages/" + selectedLanguageId);
        apiService.deleteLanguage(selectedLanguageId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d("AdminActivity", "Delete language response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.language_deleted, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Delete language failed", t);
            }
        });
    }

    private void createCourse() {
        String title = courseTitleET.getText().toString().trim();
        String langName = languageSpinnerForCourses.getText().toString().trim();
        
        if (title.isEmpty() || langName.isEmpty()) {
            Toast.makeText(this, "Course title and Language are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Long langId = null;
        String resolvedLangName = null;
        for (LanguageModel l : languagesList) {
            String name = l.getName() != null ? l.getName() : l.getLanguageName();
            if (langName.equals(name)) {
                langId = l.getId();
                resolvedLangName = name;
                break;
            }
        }
        
        if (langId == null) {
            Toast.makeText(this, "Please select a valid language", Toast.LENGTH_SHORT).show();
            return;
        }

        Course course = new Course();
        course.setTitle(title);
        course.setLanguageName(resolvedLangName);
        course.setLanguageId(langId);
        course.setDescription(courseDescET.getText().toString().trim());
        course.setModerationNote(courseModerationNoteET.getText().toString().trim());
        course.setReviewedAt(courseReviewedAtET.getText().toString().trim());
        course.setSubmissionStatus(courseStatusET.getText().toString().trim().isEmpty() ? "DRAFT" : courseStatusET.getText().toString().trim());
        course.setFlag(courseFlagET.getText().toString().trim());

        Log.d("AdminActivity", "Creating course at: " + BuildConfig.BASE_URL + "api/courses");
        apiService.createCourse(course).enqueue(new Callback<Course>() {
            @Override
            public void onResponse(@NonNull Call<Course> call, @NonNull Response<Course> response) {
                Log.d("AdminActivity", "Create course response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.course_created, Toast.LENGTH_SHORT).show();
                    clearCourseFields();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Course> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Create course failed", t);
            }
        });
    }

    private void clearCourseFields() {
        selectedCourseId = null;
        courseTitleET.setText("");
        courseDescET.setText("");
        courseModerationNoteET.setText("");
        courseReviewedAtET.setText("");
        courseStatusET.setText("");
        courseFlagET.setText("");
        languageSpinnerForCourses.setText("", false);
    }

    private void updateCourse() {
        if (selectedCourseId == null) {
            Toast.makeText(this, "Select a course to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String title = courseTitleET.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Course title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Course course = new Course();
        course.setId(selectedCourseId);
        course.setTitle(title);
        course.setDescription(courseDescET.getText().toString().trim());
        course.setModerationNote(courseModerationNoteET.getText().toString().trim());
        course.setReviewedAt(courseReviewedAtET.getText().toString().trim());
        course.setSubmissionStatus(courseStatusET.getText().toString().trim());
        course.setFlag(courseFlagET.getText().toString().trim());
        
        String langName = languageSpinnerForCourses.getText().toString().trim();
        for (LanguageModel l : languagesList) {
            String name = l.getName() != null ? l.getName() : l.getLanguageName();
            if (langName.equals(name)) {
                course.setLanguageId(l.getId());
                course.setLanguageName(name);
                break;
            }
        }

        Log.d("AdminActivity", "Updating course at: " + BuildConfig.BASE_URL + "api/courses/" + selectedCourseId);
        apiService.updateCourse(selectedCourseId, course).enqueue(new Callback<Course>() {
            @Override
            public void onResponse(@NonNull Call<Course> call, @NonNull Response<Course> response) {
                Log.d("AdminActivity", "Update course response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.course_updated, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Course> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Update course failed", t);
            }
        });
    }

    private void deleteCourse() {
        if (selectedCourseId == null) return;
        Log.d("AdminActivity", "Deleting course at: " + BuildConfig.BASE_URL + "api/courses/" + selectedCourseId);
        apiService.deleteCourse(selectedCourseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d("AdminActivity", "Delete course response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Course deleted", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Delete course failed", t);
            }
        });
    }

    private void createLesson() {
        String title = lessonTitleET.getText().toString().trim();
        String courseTitle = courseSpinnerForLessons.getText().toString().trim();
        
        if (title.isEmpty() || courseTitle.isEmpty()) {
            Toast.makeText(this, "Lesson title and Course are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Long courseId = null;
        for (Course c : coursesList) {
            if (courseTitle.equals(c.getTitle())) {
                courseId = c.getId();
                break;
            }
        }
        
        if (courseId == null) {
            Toast.makeText(this, "Please select a valid course", Toast.LENGTH_SHORT).show();
            return;
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setCourseId(courseId);
        lesson.setDescription(lessonDescriptionET.getText().toString().trim().isEmpty() ? title : lessonDescriptionET.getText().toString().trim());
        lesson.setContent(lessonContentET.getText().toString().trim());
        lesson.setTopic(lessonTopicET.getText().toString().trim());
        lesson.setModerationNote(lessonModerationNoteET.getText().toString().trim());
        lesson.setReviewedAt(lessonReviewedAtET.getText().toString().trim());
        lesson.setSubmissionStatus(lessonStatusET.getText().toString().trim().isEmpty() ? "DRAFT" : lessonStatusET.getText().toString().trim());

        Log.d("AdminActivity", "Creating lesson at: " + BuildConfig.BASE_URL + "api/lessons");
        apiService.createLesson(lesson).enqueue(new Callback<Lesson>() {
            @Override
            public void onResponse(@NonNull Call<Lesson> call, @NonNull Response<Lesson> response) {
                Log.d("AdminActivity", "Create lesson response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.lesson_created, Toast.LENGTH_SHORT).show();
                    clearLessonFields();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Lesson> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Create lesson failed", t);
            }
        });
    }

    private void clearLessonFields() {
        selectedLessonId = null;
        lessonTitleET.setText("");
        lessonDescriptionET.setText("");
        lessonContentET.setText("");
        lessonTopicET.setText("");
        lessonModerationNoteET.setText("");
        lessonReviewedAtET.setText("");
        lessonStatusET.setText("");
        courseSpinnerForLessons.setText("", false);
    }

    private void updateLesson() {
        if (selectedLessonId == null) {
            Toast.makeText(this, "Select a lesson to update", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = lessonTitleET.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Lesson title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Lesson lesson = new Lesson();
        lesson.setId(selectedLessonId);
        lesson.setTitle(title);
        lesson.setDescription(lessonDescriptionET.getText().toString().trim());
        lesson.setContent(lessonContentET.getText().toString().trim());
        lesson.setTopic(lessonTopicET.getText().toString().trim());
        lesson.setModerationNote(lessonModerationNoteET.getText().toString().trim());
        lesson.setReviewedAt(lessonReviewedAtET.getText().toString().trim());
        lesson.setSubmissionStatus(lessonStatusET.getText().toString().trim());

        String courseTitle = courseSpinnerForLessons.getText().toString().trim();
        for (Course c : coursesList) {
            if (courseTitle.equals(c.getTitle())) {
                lesson.setCourseId(c.getId());
                break;
            }
        }

        Log.d("AdminActivity", "Updating lesson at: " + BuildConfig.BASE_URL + "api/lessons/" + selectedLessonId);
        apiService.updateLesson(selectedLessonId, lesson).enqueue(new Callback<Lesson>() {
            @Override
            public void onResponse(@NonNull Call<Lesson> call, @NonNull Response<Lesson> response) {
                Log.d("AdminActivity", "Update lesson response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.lesson_updated, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Lesson> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Update lesson failed", t);
            }
        });
    }

    private void deleteLesson(Lesson item) {
        Log.d("AdminActivity", "Deleting lesson at: " + BuildConfig.BASE_URL + "api/lessons/" + item.getId());
        apiService.deleteLesson(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d("AdminActivity", "Delete lesson response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.lesson_deleted, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Delete lesson failed", t);
            }
        });
    }

    private void populateExerciseFields(Question ex) {
        selectedExerciseId = (long) ex.getId();
        exercisePromptET.setText(ex.getQuestion());
        exerciseQuestionTextET.setText(ex.getQuestionText());
        exerciseAnsET.setText(ex.getAnswer());
        exerciseOptionsET.setText(ex.getOptions());
        exerciseHintET.setText(ex.getHint());
        exerciseTypeSpinner.setText(ex.getType(), false);
        
        exercisePhoneticET.setText(ex.getPhonetic());
        exerciseExampleET.setText(ex.getExampleSentence());
        exerciseTopicET.setText(ex.getTopic());
        exerciseAudioET.setText(ex.getAudioPath());
        exerciseSubTypeET.setText(ex.getSubType());
        exerciseMetadataET.setText(ex.getMetadata());

        // Resolve Lesson in spinner
        if (lessonSpinnerForExercises != null) {
            for (Lesson l : lessonsList) {
                if (Objects.equals(l.getId(), ex.getLessonId())) {
                    lessonSpinnerForExercises.setText(l.getTitle(), false);
                    break;
                }
            }
        }
        
        Toast.makeText(this, getString(R.string.editing_exercise_format, ex.getQuestion()), Toast.LENGTH_SHORT).show();
    }

    private void createExercise() {
        String prompt = exercisePromptET.getText().toString().trim();
        String type = exerciseTypeSpinner.getText().toString().trim();
        String lessonTitle = lessonSpinnerForExercises.getText().toString().trim();
        
        if (prompt.isEmpty() || type.isEmpty() || lessonTitle.isEmpty()) {
            Toast.makeText(this, "Prompt, Type and Lesson are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Question q = new Question();
        q.setQuestion(prompt);
        q.setQuestionText(exerciseQuestionTextET.getText().toString().trim());
        q.setAnswer(exerciseAnsET.getText().toString().trim());
        q.setOptions(exerciseOptionsET.getText().toString().trim());
        q.setHint(exerciseHintET.getText().toString().trim());
        q.setType(type);
        
        q.setPhonetic(exercisePhoneticET.getText().toString().trim());
        q.setExampleSentence(exerciseExampleET.getText().toString().trim());
        q.setTopic(exerciseTopicET.getText().toString().trim());
        q.setCategory(exerciseTopicET.getText().toString().trim());
        q.setAudioPath(exerciseAudioET.getText().toString().trim());
        q.setSubType(exerciseSubTypeET.getText().toString().trim());
        q.setMetadata(exerciseMetadataET.getText().toString().trim());
        
        Long lessonId = null;
        for (Lesson l : lessonsList) {
            if (lessonTitle.equals(l.getTitle())) {
                lessonId = l.getId();
                break;
            }
        }
        
        if (lessonId == null) {
            Toast.makeText(this, "Please select a valid lesson", Toast.LENGTH_SHORT).show();
            return;
        }
        q.setLessonId(lessonId);
        
        Log.d("AdminActivity", "Creating exercise at: " + BuildConfig.BASE_URL + "api/exercises");
        apiService.addQuestion(q).enqueue(new Callback<Question>() {
            @Override
            public void onResponse(@NonNull Call<Question> call, @NonNull Response<Question> response) {
                Log.d("AdminActivity", "Create exercise response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.exercise_created, Toast.LENGTH_SHORT).show();
                    clearExerciseFields();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Question> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Create exercise failed", t);
            }
        });
    }

    private void clearExerciseFields() {
        selectedExerciseId = null;
        exercisePromptET.setText("");
        exerciseQuestionTextET.setText("");
        exerciseAnsET.setText("");
        exerciseOptionsET.setText("");
        exerciseHintET.setText("");
        exercisePhoneticET.setText("");
        exerciseExampleET.setText("");
        exerciseTopicET.setText("");
        exerciseAudioET.setText("");
        exerciseSubTypeET.setText("");
        exerciseMetadataET.setText("");
        exerciseTypeSpinner.setText("", false);
        lessonSpinnerForExercises.setText("", false);
    }

    private void updateExercise() {
        if (selectedExerciseId == null) {
            Toast.makeText(this, "Select an exercise to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String prompt = exercisePromptET.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Question prompt is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Question q = new Question();
        q.setId(selectedExerciseId);
        q.setQuestion(prompt);
        q.setQuestionText(exerciseQuestionTextET.getText().toString().trim());
        q.setAnswer(exerciseAnsET.getText().toString().trim());
        q.setOptions(exerciseOptionsET.getText().toString().trim());
        q.setHint(exerciseHintET.getText().toString().trim());
        q.setType(exerciseTypeSpinner.getText().toString().trim());
        
        q.setPhonetic(exercisePhoneticET.getText().toString().trim());
        q.setExampleSentence(exerciseExampleET.getText().toString().trim());
        q.setTopic(exerciseTopicET.getText().toString().trim());
        q.setCategory(exerciseTopicET.getText().toString().trim());
        q.setAudioPath(exerciseAudioET.getText().toString().trim());
        q.setSubType(exerciseSubTypeET.getText().toString().trim());
        q.setMetadata(exerciseMetadataET.getText().toString().trim());

        String lessonTitle = lessonSpinnerForExercises.getText().toString().trim();
        for (Lesson l : lessonsList) {
            if (lessonTitle.equals(l.getTitle())) {
                q.setLessonId(l.getId());
                break;
            }
        }

        Log.d("AdminActivity", "Updating exercise at: " + BuildConfig.BASE_URL + "api/exercises/" + selectedExerciseId);
        apiService.editExercise(q.getId(), q).enqueue(new Callback<Question>() {
            @Override
            public void onResponse(@NonNull Call<Question> call, @NonNull Response<Question> response) {
                Log.d("AdminActivity", "Update exercise response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.exercise_updated, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Question> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Update exercise failed", t);
            }
        });
    }

    private void deleteExercise(Question item) {
        Log.d("AdminActivity", "Deleting exercise at: " + BuildConfig.BASE_URL + "api/exercises/" + item.getId());
        apiService.deleteExercise((long) item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d("AdminActivity", "Delete exercise response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.exercise_deleted, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Delete exercise failed", t);
            }
        });
    }

    private void populateVocabFields(VocabularyItem item) {
        selectedVocabId = item.getId();
        vocabWordET.setText(item.getWord());
        vocabTargetET.setText(item.getWordTarget());
        vocabPhoneticET.setText(item.getPhonetic());
        vocabTranslationET.setText(item.getTranslation());
        vocabExampleET.setText(item.getExampleSentence());
        vocabTopicET.setText(item.getTopic());
        vocabAudioPathET.setText(item.getAudioPath());
        
        for (Lesson l : lessonsList) {
            if (Objects.equals(l.getId(), item.getLessonId())) {
                vocabLessonSpinner.setText(l.getTitle(), false);
                break;
            }
        }
    }

    private void createVocab() {
        String word = vocabWordET.getText().toString().trim();
        String translation = vocabTranslationET.getText().toString().trim();
        if (word.isEmpty() || translation.isEmpty()) {
            Toast.makeText(this, R.string.word_translation_required, Toast.LENGTH_SHORT).show();
            return;
        }
        
        VocabularyItem item = new VocabularyItem();
        item.setWord(word);
        item.setWordTarget(vocabTargetET.getText().toString().trim());
        item.setPhonetic(vocabPhoneticET.getText().toString().trim());
        item.setTranslation(translation);
        item.setExampleSentence(vocabExampleET.getText().toString().trim());
        item.setTopic(vocabTopicET.getText().toString().trim());
        item.setAudioPath(vocabAudioPathET.getText().toString().trim());

        // Resolve IDs from Spinners
        String lTitle = vocabLessonSpinner.getText().toString();

        for (Lesson l : lessonsList) {
            if (lTitle.equals(l.getTitle())) {
                item.setLessonId(l.getId());
                item.setLessonTitle(l.getTitle());
                break;
            }
        }

        if (item.getLessonId() == null) {
            Toast.makeText(this, R.string.no_lesson, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AdminActivity", "Creating vocabulary at: " + BuildConfig.BASE_URL + "api/vocabulary");
        apiService.createVocabulary(item).enqueue(new Callback<VocabularyItem>() {
            @Override
            public void onResponse(@NonNull Call<VocabularyItem> call, @NonNull Response<VocabularyItem> response) {
                Log.d("AdminActivity", "Create vocabulary response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.vocab_created, Toast.LENGTH_SHORT).show();
                    clearVocabFields();
                    loadVocabulary();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<VocabularyItem> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Create vocabulary failed", t);
            }
        });
    }

    private void clearVocabFields() {
        selectedVocabId = null;
        vocabWordET.setText("");
        vocabTargetET.setText("");
        vocabPhoneticET.setText("");
        vocabTranslationET.setText("");
        vocabExampleET.setText("");
        vocabTopicET.setText("");
        vocabAudioPathET.setText("");
        vocabLessonSpinner.setText("", false);
    }

    private void updateVocab() {
        if (selectedVocabId == null) {
            Toast.makeText(this, "Select a vocabulary item to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String word = vocabWordET.getText().toString().trim();
        String translation = vocabTranslationET.getText().toString().trim();
        if (word.isEmpty() || translation.isEmpty()) {
            Toast.makeText(this, R.string.word_translation_required, Toast.LENGTH_SHORT).show();
            return;
        }

        VocabularyItem item = new VocabularyItem();
        item.setId(selectedVocabId);
        item.setWord(word);
        item.setWordTarget(vocabTargetET.getText().toString().trim());
        item.setPhonetic(vocabPhoneticET.getText().toString().trim());
        item.setTranslation(translation);
        item.setExampleSentence(vocabExampleET.getText().toString().trim());
        item.setTopic(vocabTopicET.getText().toString().trim());
        item.setAudioPath(vocabAudioPathET.getText().toString().trim());

        // Resolve IDs for update
        String lTitle = vocabLessonSpinner.getText().toString().trim();

        for (Lesson l : lessonsList) {
            if (lTitle.equals(l.getTitle())) {
                item.setLessonId(l.getId());
                item.setLessonTitle(l.getTitle());
                break;
            }
        }

        if (item.getLessonId() == null) {
            Toast.makeText(this, R.string.no_lesson, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AdminActivity", "Updating vocabulary at: " + BuildConfig.BASE_URL + "api/vocabulary/" + selectedVocabId);
        apiService.updateVocabulary(selectedVocabId, item).enqueue(new Callback<VocabularyItem>() {
            @Override
            public void onResponse(@NonNull Call<VocabularyItem> call, @NonNull Response<VocabularyItem> response) {
                Log.d("AdminActivity", "Update vocabulary response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.vocab_updated, Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<VocabularyItem> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Update vocabulary failed", t);
            }
        });
    }

    private void deleteVocab(VocabularyItem item) {
        Log.d("AdminActivity", "Deleting vocabulary at: " + BuildConfig.BASE_URL + "api/vocabulary/" + item.getId());
        apiService.deleteVocabulary(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d("AdminActivity", "Delete vocabulary response: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.vocab_deleted, Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                    loadInitialData();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("AdminActivity", "Delete vocabulary failed", t);
            }
        });
    }

    private void deleteUser(User user) {
        apiService.deleteUser((long) user.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.user_deleted, Toast.LENGTH_SHORT).show();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }

    private void promoteUser(User user) {
        user.setRole("ADMIN");
        apiService.editUser((long) user.getId(), user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, R.string.user_promoted, Toast.LENGTH_SHORT).show();
                    loadDashboardStats();
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
        });
    }

    private void viewUserHistory(User user) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("userId", (long) user.getId());
        startActivity(intent);
    }
}
