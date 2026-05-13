package com.app.rualingoapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private TextView totalQuestionsText, totalUsersText, adminTitle;
    private View overviewSection, languagesSection, coursesSection, exercisesSection, vocabularySection;
    private View navOverview, navLanguages, navCourses, navExercises, navVocabulary;
    private ImageView iconOverview, iconLanguages, iconCourses, iconExercises, iconVocabulary;
    private TextView textOverview, textLanguages, textCourses, textExercises, textVocabulary;

    // Courses & Lessons Sub-navigation
    private com.google.android.material.tabs.TabLayout coursesSubTabLayout;
    private View coursesSubSection, lessonsSubSection;
    
    // Form fields
    private TextInputEditText searchLanguageET, searchCourseET, searchLessonET;
    private TextInputEditText languageNameET, provinceET, districtET, clanET, flagET;
    private TextInputEditText courseTitleET, courseDescET, courseModerationNoteET, courseReviewedAtET;
    private TextInputEditText lessonTitleET, lessonContentET, lessonModerationNoteET, lessonReviewedAtET, lessonStatusET;
    private TextInputEditText exercisePromptET, exerciseQuestionTextET, exerciseAnsET, exerciseOptionsET, exerciseHintET;
    private TextInputEditText vocabWordET, vocabTargetET, vocabPhoneticET, vocabTranslationET, vocabExampleET, vocabTopicET;
    
    // Spinners
    private AutoCompleteTextView langSpinnerForCourses, courseSpinnerForLessons, lessonSpinnerForExercises, exerciseTypeSpinner;
    private AutoCompleteTextView langSpinnerForExercises, courseSpinnerForExercises, vocabLangSpinner, vocabCourseSpinner, vocabLessonSpinner;
    
    private ApiService apiService;
    private SessionManager sessionManager;
    
    private List<LanguageModel> languagesList = new ArrayList<>();
    private List<Course> coursesList = new ArrayList<>();
    private List<Lesson> lessonsList = new ArrayList<>();
    private List<VocabularyItem> vocabularyList = new ArrayList<>();
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
        setupSearch();
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
        TextView profileName = findViewById(R.id.adminProfileName);
        if (sessionManager != null && profileName != null) {
            String fullName = sessionManager.getFirstName() + " " + sessionManager.getSecondName();
            profileName.setText(fullName.trim().isEmpty() ? sessionManager.getUsername() : fullName);
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
        coursesSubTabLayout = findViewById(R.id.coursesSubTabLayout);
        coursesSubSection = findViewById(R.id.coursesSubSection);
        lessonsSubSection = findViewById(R.id.lessonsSubSection);

        coursesSubTabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    coursesSubSection.setVisibility(View.VISIBLE);
                    lessonsSubSection.setVisibility(View.GONE);
                } else {
                    coursesSubSection.setVisibility(View.GONE);
                    lessonsSubSection.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });

        // Dashboard
        totalQuestionsText = findViewById(R.id.totalQuestionsText);
        totalUsersText = findViewById(R.id.totalUsersText);
        adminTitle = findViewById(R.id.adminTitle);

        // Profile Card
        View profileCard = findViewById(R.id.adminProfileCard);
        TextView profileName = findViewById(R.id.adminProfileName);
        TextView profileRole = findViewById(R.id.adminProfileRole);

        if (sessionManager != null) {
            String fullName = sessionManager.getFirstName() + " " + sessionManager.getSecondName();
            profileName.setText(fullName.trim().isEmpty() ? sessionManager.getUsername() : fullName);
            profileRole.setText("Administrator");
            profileCard.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        // Bottom Nav
        navOverview = findViewById(R.id.navAdminOverview);
        navLanguages = findViewById(R.id.navAdminLanguages);
        navCourses = findViewById(R.id.navAdminCourses);
        navExercises = findViewById(R.id.navAdminExercises);
        navVocabulary = findViewById(R.id.navAdminVocabulary);

        iconOverview = findViewById(R.id.iconOverview);
        iconLanguages = findViewById(R.id.iconLanguages);
        iconCourses = findViewById(R.id.iconCourses);
        iconExercises = findViewById(R.id.iconExercises);
        iconVocabulary = findViewById(R.id.iconVocabulary);

        textOverview = findViewById(R.id.textOverview);
        textLanguages = findViewById(R.id.textLanguages);
        textCourses = findViewById(R.id.textCourses);
        textExercises = findViewById(R.id.textExercises);
        textVocabulary = findViewById(R.id.textVocabulary);

        // Forms
        searchLanguageET = findViewById(R.id.searchLanguageEditText);
        languageNameET = findViewById(R.id.languageNameEditText);
        provinceET = findViewById(R.id.provinceEditText);
        districtET = findViewById(R.id.districtEditText);
        clanET = findViewById(R.id.clanEditText);
        flagET = findViewById(R.id.flagEditText);
        
        searchCourseET = findViewById(R.id.searchCourseEditText);
        courseTitleET = findViewById(R.id.courseTitleEditText);
        courseDescET = findViewById(R.id.courseDescriptionInput);
        courseModerationNoteET = findViewById(R.id.courseModerationNoteInput);
        courseReviewedAtET = findViewById(R.id.courseReviewedAtInput);
        
        searchLessonET = findViewById(R.id.searchLessonEditText);
        lessonTitleET = findViewById(R.id.lessonTitleEditText);
        lessonContentET = findViewById(R.id.lessonDescEditText);
        lessonModerationNoteET = findViewById(R.id.lessonModerationNoteEditText);
        lessonReviewedAtET = findViewById(R.id.lessonReviewedAtEditText);
        lessonStatusET = findViewById(R.id.lessonStatusEditText);

        exercisePromptET = findViewById(R.id.exerciseQuestionEditText);
        exerciseQuestionTextET = findViewById(R.id.exerciseQuestionTextEditText);
        exerciseAnsET = findViewById(R.id.correctAnsEditText);
        exerciseOptionsET = findViewById(R.id.optionsEditText);
        exerciseHintET = findViewById(R.id.exerciseHintEditText);

        // Vocabulary Forms
        vocabWordET = findViewById(R.id.vocabWordEditText);
        vocabTargetET = findViewById(R.id.vocabTargetEditText);
        vocabPhoneticET = findViewById(R.id.vocabPhoneticEditText);
        vocabTranslationET = findViewById(R.id.vocabTranslationEditText);
        vocabExampleET = findViewById(R.id.vocabExampleEditText);
        vocabTopicET = findViewById(R.id.vocabTopicEditText);

        // Spinners
        langSpinnerForCourses = findViewById(R.id.languageSpinnerForCourses);
        courseSpinnerForLessons = findViewById(R.id.courseSpinnerForLessons);
        lessonSpinnerForExercises = findViewById(R.id.lessonSpinnerForExercises);
        langSpinnerForExercises = findViewById(R.id.languageSpinnerForExercises);
        courseSpinnerForExercises = findViewById(R.id.courseSpinnerForExercises);
        exerciseTypeSpinner = findViewById(R.id.exerciseTypeSpinner);
        vocabLangSpinner = findViewById(R.id.vocabLanguageSpinner);
        vocabCourseSpinner = findViewById(R.id.vocabCourseSpinner);
        vocabLessonSpinner = findViewById(R.id.vocabLessonSpinner);

        // RecyclerViews
        ((RecyclerView) findViewById(R.id.userModerationRecyclerView)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.adminModerationRecyclerView)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.languagesRecyclerView)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.coursesRecyclerView)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.lessonsRecyclerView)).setLayoutManager(new LinearLayoutManager(this));
        ((RecyclerView) findViewById(R.id.exercisesRecyclerView)).setLayoutManager(new LinearLayoutManager(this));
        
        RecyclerView vocabRV = findViewById(R.id.vocabRecyclerView);
        vocabRV.setLayoutManager(new LinearLayoutManager(this));
        vocabAdapter = new VocabularyManageAdapter(vocabularyList, languagesList, coursesList, lessonsList, new VocabularyManageAdapter.OnVocabActionListener() {
            @Override public void onEdit(VocabularyItem item) { populateVocabFields(item); }
            @Override public void onDelete(VocabularyItem item) { deleteVocab(item); }
        });
        vocabRV.setAdapter(vocabAdapter);
    }

    private void setupBottomNav() {
        navOverview.setOnClickListener(v -> switchSection("overview"));
        navLanguages.setOnClickListener(v -> switchSection("languages"));
        navCourses.setOnClickListener(v -> switchSection("courses"));
        navExercises.setOnClickListener(v -> switchSection("exercises"));
        navVocabulary.setOnClickListener(v -> switchSection("vocabulary"));
    }

    private void switchSection(String section) {
        int gray = ContextCompat.getColor(this, R.color.duo_gray);
        int blue = ContextCompat.getColor(this, R.color.duo_blue);

        // Reset
        ImageView[] icons = {iconOverview, iconLanguages, iconCourses, iconExercises, iconVocabulary};
        TextView[] texts = {textOverview, textLanguages, textCourses, textExercises, textVocabulary};
        for (ImageView i : icons) i.setColorFilter(gray);
        for (TextView t : texts) t.setTextColor(gray);

        overviewSection.setVisibility(View.GONE);
        languagesSection.setVisibility(View.GONE);
        coursesSection.setVisibility(View.GONE);
        exercisesSection.setVisibility(View.GONE);
        vocabularySection.setVisibility(View.GONE);

        switch (section) {
            case "overview" -> {
                overviewSection.setVisibility(View.VISIBLE);
                iconOverview.setColorFilter(blue);
                textOverview.setTextColor(blue);
                adminTitle.setText("Admin Center");
            }
            case "languages" -> {
                languagesSection.setVisibility(View.VISIBLE);
                iconLanguages.setColorFilter(blue);
                textLanguages.setTextColor(blue);
                adminTitle.setText("Languages");
            }
            case "courses" -> {
                coursesSection.setVisibility(View.VISIBLE);
                iconCourses.setColorFilter(blue);
                textCourses.setTextColor(blue);
                adminTitle.setText("Courses & Lessons");
            }
            case "exercises" -> {
                exercisesSection.setVisibility(View.VISIBLE);
                iconExercises.setColorFilter(blue);
                textExercises.setTextColor(blue);
                adminTitle.setText("Exercises");
            }
            case "vocabulary" -> {
                vocabularySection.setVisibility(View.VISIBLE);
                iconVocabulary.setColorFilter(blue);
                textVocabulary.setTextColor(blue);
                adminTitle.setText("Vocabulary");
                loadVocabulary(); // Refresh list when switching to tab
            }
        }
    }

    private void setupLogout() {
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupCrudActions() {
        findViewById(R.id.createLanguageButton).setOnClickListener(v -> createLanguage());
        findViewById(R.id.updateLanguageButton).setOnClickListener(v -> updateLanguage());

        findViewById(R.id.createCourseButton).setOnClickListener(v -> createCourse());
        findViewById(R.id.updateCourseButton).setOnClickListener(v -> updateCourse());

        findViewById(R.id.createLessonButton).setOnClickListener(v -> createLesson());
        findViewById(R.id.updateLessonButton).setOnClickListener(v -> updateLesson());

        findViewById(R.id.createExerciseButton).setOnClickListener(v -> createExercise());
        findViewById(R.id.updateExerciseButton).setOnClickListener(v -> updateExercise());

        findViewById(R.id.createVocabButton).setOnClickListener(v -> createVocab());
        findViewById(R.id.updateVocabButton).setOnClickListener(v -> updateVocab());

        setupDatePickers();
    }

    private void setupCascadingSpinners() {
        langSpinnerForExercises.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = (String) parent.getItemAtPosition(position);
            updateCourseSpinnerForExercises(selectedLang);
            courseSpinnerForExercises.setText("");
            lessonSpinnerForExercises.setText("");
        });

        courseSpinnerForExercises.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = (String) parent.getItemAtPosition(position);
            updateLessonSpinnerForExercises(selectedCourse);
            lessonSpinnerForExercises.setText("");
        });

        vocabLangSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = (String) parent.getItemAtPosition(position);
            updateCourseSpinnerForVocab(selectedLang);
            vocabCourseSpinner.setText("");
            vocabLessonSpinner.setText("");
        });

        vocabCourseSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = (String) parent.getItemAtPosition(position);
            updateLessonSpinnerForVocab(selectedCourse);
            vocabLessonSpinner.setText("");
        });
    }

    private void updateCourseSpinnerForVocab(String langName) {
        List<String> titles = new ArrayList<>();
        for (Course c : coursesList) {
            if (c.getLanguageName() != null && c.getLanguageName().equals(langName)) {
                titles.add(c.getTitle());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        vocabCourseSpinner.setAdapter(adapter);
    }

    private void updateLessonSpinnerForVocab(String courseTitle) {
        List<String> titles = new ArrayList<>();
        Long courseId = null;
        for (Course c : coursesList) {
            if (c.getTitle() != null && c.getTitle().equalsIgnoreCase(courseTitle)) {
                courseId = c.getId();
                break;
            }
        }
        if (courseId != null) {
            for (Lesson l : lessonsList) {
                if (l.getCourseId() != null && l.getCourseId().equals(courseId)) {
                    titles.add(l.getTitle());
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        vocabLessonSpinner.setAdapter(adapter);
    }

    private void updateCourseSpinnerForExercises(String languageName) {
        Long langId = null;
        for (LanguageModel l : languagesList) {
            String lName = l.getName() != null ? l.getName() : l.getLanguageName();
            if (lName != null && lName.equals(languageName)) {
                langId = l.getId();
                break;
            }
        }
        
        List<String> filteredTitles = new ArrayList<>();
        if (langId != null) {
            for (Course c : coursesList) {
                if (langId.equals(c.getLanguageId())) {
                    filteredTitles.add(c.getTitle());
                }
            }
        } else {
            // Fallback: if no language selected, show all
            for (Course c : coursesList) filteredTitles.add(c.getTitle());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filteredTitles);
        courseSpinnerForExercises.setAdapter(adapter);
    }

    private void updateLessonSpinnerForExercises(String courseTitle) {
        Long courseId = null;
        for (Course c : coursesList) {
            if (c.getTitle() != null && c.getTitle().equals(courseTitle)) {
                courseId = c.getId();
                break;
            }
        }

        List<String> filteredTitles = new ArrayList<>();
        if (courseId != null) {
            for (Lesson l : lessonsList) {
                if (courseId.equals(l.getCourseId())) {
                    filteredTitles.add(l.getTitle());
                }
            }
        } else {
            // Fallback: show all lessons if no course selected
            for (Lesson l : lessonsList) filteredTitles.add(l.getTitle());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filteredTitles);
        lessonSpinnerForExercises.setAdapter(adapter);
    }

    private void setupSearch() {
        searchLanguageET.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchLanguage(searchLanguageET.getText().toString());
                return true;
            }
            return false;
        });
        
        searchCourseET.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchCourse(searchCourseET.getText().toString());
                return true;
            }
            return false;
        });

        searchLessonET.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchLesson(searchLessonET.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void setupDatePickers() {
        courseReviewedAtET.setOnClickListener(v -> showDatePicker(courseReviewedAtET));
        lessonReviewedAtET.setOnClickListener(v -> showDatePicker(lessonReviewedAtET));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            et.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void searchLanguage(String query) {
        if (query.isEmpty()) return;
        for (LanguageModel lang : languagesList) {
            if (lang.getName().equalsIgnoreCase(query) || (lang.getLanguageName() != null && lang.getLanguageName().equalsIgnoreCase(query))) {
                populateLanguageFields(lang);
                return;
            }
        }
        Toast.makeText(this, "Language not found locally", Toast.LENGTH_SHORT).show();
    }

    private void searchCourse(String query) {
        if (query.isEmpty()) return;
        for (Course course : coursesList) {
            if (course.getTitle().equalsIgnoreCase(query)) {
                populateCourseFields(course);
                return;
            }
        }
        Toast.makeText(this, "Course not found locally", Toast.LENGTH_SHORT).show();
    }

    private void searchLesson(String query) {
        if (query.isEmpty()) return;
        for (Lesson lesson : lessonsList) {
            if (lesson.getTitle().equalsIgnoreCase(query)) {
                populateLessonFields(lesson);
                return;
            }
        }
        Toast.makeText(this, "Lesson not found locally", Toast.LENGTH_SHORT).show();
    }

    private void populateLanguageFields(LanguageModel lang) {
        selectedLanguageId = lang.getId();
        languageNameET.setText(lang.getName());
        provinceET.setText(lang.getProvince());
        districtET.setText(lang.getDistrict());
        clanET.setText(lang.getClan());
        flagET.setText(lang.getFlag());
        Toast.makeText(this, "Found: " + lang.getName(), Toast.LENGTH_SHORT).show();
    }

    private void populateCourseFields(Course course) {
        selectedCourseId = course.getId();
        courseTitleET.setText(course.getTitle());
        courseDescET.setText(course.getDescription());
        courseModerationNoteET.setText(course.getModerationNote());
        courseReviewedAtET.setText(course.getReviewedAt());
        
        // Populate language spinner
        if (course.getLanguageId() != null) {
            for (LanguageModel lang : languagesList) {
                if (lang.getId().equals(course.getLanguageId())) {
                    langSpinnerForCourses.setText(lang.getName(), false);
                    break;
                }
            }
        }
        Toast.makeText(this, "Found Course: " + course.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void populateLessonFields(Lesson lesson) {
        selectedLessonId = lesson.getId();
        lessonTitleET.setText(lesson.getTitle());
        lessonContentET.setText(lesson.getContent());
        lessonModerationNoteET.setText(lesson.getModerationNote());
        lessonReviewedAtET.setText(lesson.getReviewedAt());
        lessonStatusET.setText(lesson.getSubmissionStatus());
        
        // Populate course spinner
        if (lesson.getCourseId() != null) {
            for (Course c : coursesList) {
                if (c.getId().equals(lesson.getCourseId())) {
                    courseSpinnerForLessons.setText(c.getTitle(), false);
                    break;
                }
            }
        }
        Toast.makeText(this, "Found Lesson: " + lesson.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void loadDashboardStats() {
        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalQuestionsText.setText(String.valueOf(response.body().size()));
                    updateCourseList(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {}
        });

        apiService.fetchAllUsers().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalUsersText.setText(String.valueOf(response.body().size()));
                    updateModerationLists(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {}
        });
    }

    private void loadInitialData() {
        apiService.getLanguages().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<LanguageModel>> call, @NonNull Response<List<LanguageModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    languagesList.clear();
                    languagesList.addAll(response.body());
                    updateLanguageSpinner();
                    updateLanguageList(languagesList);
                    if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<LanguageModel>> call, @NonNull Throwable t) {}
        });

        apiService.getLessons().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Lesson>> call, @NonNull Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lessonsList.clear();
                    lessonsList.addAll(response.body());
                    updateLessonSpinner();
                    updateLessonList(lessonsList);
                    if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Lesson>> call, @NonNull Throwable t) {}
        });
        
        apiService.getExercises().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateExerciseList(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {}
        });

        loadVocabulary();
        updateExerciseTypeSpinner();
    }

    private void loadVocabulary() {
        apiService.getVocabulary(null, null).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<VocabularyItem>> call, @NonNull Response<List<VocabularyItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vocabularyList.clear();
                    List<VocabularyItem> body = response.body();
                    
                    // Sort by Language ID then Course ID then Lesson ID then Topic
                    Collections.sort(body, (v1, v2) -> {
                        long l1 = v1.getLanguageId() != null ? v1.getLanguageId() : 0L;
                        long l2 = v2.getLanguageId() != null ? v2.getLanguageId() : 0L;
                        if (l1 != l2) return Long.compare(l1, l2);
                        
                        long c1 = v1.getCourseId() != null ? v1.getCourseId() : 0L;
                        long c2 = v2.getCourseId() != null ? v2.getCourseId() : 0L;
                        if (c1 != c2) return Long.compare(c1, c2);

                        long les1 = v1.getLessonId() != null ? v1.getLessonId() : 0L;
                        long les2 = v2.getLessonId() != null ? v2.getLessonId() : 0L;
                        if (les1 != les2) return Long.compare(les1, les2);

                        String t1 = v1.getTopic() != null ? v1.getTopic() : "";
                        String t2 = v2.getTopic() != null ? v2.getTopic() : "";
                        return t1.compareToIgnoreCase(t2);
                    });
                    
                    vocabularyList.addAll(body);
                    if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<List<VocabularyItem>> call, @NonNull Throwable t) {}
        });
    }

    private void updateExerciseTypeSpinner() {
        String[] types = {"Fill in the blank", "Multiple choice"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        exerciseTypeSpinner.setAdapter(adapter);
    }

    private void updateLanguageSpinner() {
        List<String> names = new ArrayList<>();
        for (LanguageModel l : languagesList) {
            String displayName = l.getName() != null ? l.getName() : l.getLanguageName();
            if (displayName != null) names.add(displayName);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        langSpinnerForCourses.setAdapter(adapter);
        langSpinnerForExercises.setAdapter(adapter);
        vocabLangSpinner.setAdapter(adapter);
    }

    private void updateCourseSpinner() {
        List<String> titles = new ArrayList<>();
        for (Course c : coursesList) titles.add(c.getTitle());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        courseSpinnerForLessons.setAdapter(adapter);
        courseSpinnerForExercises.setAdapter(adapter);
        vocabCourseSpinner.setAdapter(adapter);
    }

    private void updateLessonSpinner() {
        List<String> titles = new ArrayList<>();
        for (Lesson l : lessonsList) titles.add(l.getTitle());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        lessonSpinnerForExercises.setAdapter(adapter);
    }

    private void updateModerationLists(List<User> allUsers) {
        List<User> admins = new ArrayList<>();
        List<User> regularUsers = new ArrayList<>();

        for (User u : allUsers) {
            String role = u.getRole();
            android.util.Log.d("AdminActivity", "User: " + u.getUsername() + ", Role: " + role + ", RoleID: " + u.getRoleId());

            // Separate based on numeric values from backend: 1 for Admin, 2 for User
            if ("1".equals(role) || (role != null && role.toUpperCase().contains("ADMIN"))) {
                admins.add(u);
            } else {
                regularUsers.add(u);
            }
        }

        UserModerationAdapter userAdapter = new UserModerationAdapter(regularUsers, new UserModerationAdapter.OnUserActionListener() {
            @Override public void onDeleteClick(User user) { deleteUser(user); }
            @Override public void onPromoteClick(User user) { promoteUser(user); }
        });
        ((RecyclerView) findViewById(R.id.userModerationRecyclerView)).setAdapter(userAdapter);

        UserModerationAdapter adminAdapter = new UserModerationAdapter(admins, new UserModerationAdapter.OnUserActionListener() {
            @Override public void onDeleteClick(User user) { deleteUser(user); }
            @Override public void onPromoteClick(User user) { promoteUser(user); }
        });
        ((RecyclerView) findViewById(R.id.adminModerationRecyclerView)).setAdapter(adminAdapter);
    }

    private void updateLanguageList(List<LanguageModel> languages) {
        LanguageDetailAdapter adapter = new LanguageDetailAdapter(languages, this::populateLanguageFields);
        ((RecyclerView) findViewById(R.id.languagesRecyclerView)).setAdapter(adapter);
    }

    private void updateCourseList(List<Course> courses) {
        this.coursesList.clear();
        this.coursesList.addAll(courses);
        updateCourseSpinner();
        CourseDetailAdapter adapter = new CourseDetailAdapter(this.coursesList, this::populateCourseFields);
        ((RecyclerView) findViewById(R.id.coursesRecyclerView)).setAdapter(adapter);
        if (vocabAdapter != null) vocabAdapter.notifyDataSetChanged();
    }

    private void updateLessonList(List<Lesson> lessons) {
        this.lessonsList = lessons;
        updateLessonSpinner();
        LessonManageAdapter adapter = new LessonManageAdapter(lessons, new LessonManageAdapter.OnLessonActionListener() {
            @Override public void onEdit(Lesson item) { populateLessonFields(item); }
            @Override public void onDelete(Lesson item) { deleteLesson(item); }
        });
        ((RecyclerView) findViewById(R.id.lessonsRecyclerView)).setAdapter(adapter);
    }

    private void updateExerciseList(List<Question> exercises) {
        // Sort by lesson ID
        Collections.sort(exercises, (q1, q2) -> {
            Long l1 = q1.getLessonId();
            Long l2 = q2.getLessonId();
            if (l1 == null && l2 == null) return 0;
            if (l1 == null) return 1;
            if (l2 == null) return -1;
            return l1.compareTo(l2);
        });

        ExerciseManageAdapter adapter = new ExerciseManageAdapter(exercises, lessonsList, new ExerciseManageAdapter.OnExerciseActionListener() {
            @Override public void onEdit(Question item) { populateExerciseFields(item); }
            @Override public void onDelete(Question item) { deleteExercise(item); }
        });
        ((RecyclerView) findViewById(R.id.exercisesRecyclerView)).setAdapter(adapter);
    }

    private void populateExerciseFields(Question q) {
        selectedExerciseId = q.getId();
        exercisePromptET.setText(q.getQuestion());
        exerciseQuestionTextET.setText(q.getQuestionText());
        exerciseAnsET.setText(q.getAnswer());
        exerciseOptionsET.setText(q.getOptions());
        exerciseHintET.setText(q.getHint());
        exerciseTypeSpinner.setText(q.getType(), false);

        if (q.getLessonId() != null) {
            for (Lesson l : lessonsList) {
                if (l.getId().equals(q.getLessonId())) {
                    lessonSpinnerForExercises.setText(l.getTitle(), false);
                    break;
                }
            }
        }
        Toast.makeText(this, "Editing Exercise: " + q.getPrompt(), Toast.LENGTH_SHORT).show();
    }

    // CRUD Implementations
    private void createLanguage() {
        String name = languageNameET.getText().toString();
        if (name.isEmpty()) return;

        LanguageModel lang = new LanguageModel();
        lang.setName(name);
        lang.setProvince(provinceET.getText().toString());
        lang.setDistrict(districtET.getText().toString());
        lang.setClan(clanET.getText().toString());
        lang.setFlag(flagET.getText().toString());
        
        apiService.createLanguage(lang).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<LanguageModel> call, @NonNull Response<LanguageModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Language Created", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    clearLanguageFields();
                }
            }
            @Override public void onFailure(@NonNull Call<LanguageModel> call, @NonNull Throwable t) {}
        });
    }

    private void updateLanguage() {
        if (selectedLanguageId == null) {
            Toast.makeText(this, "No language selected to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String name = languageNameET.getText().toString();
        if (name.isEmpty()) return;

        LanguageModel lang = new LanguageModel();
        lang.setId(selectedLanguageId);
        lang.setName(name);
        lang.setProvince(provinceET.getText().toString());
        lang.setDistrict(districtET.getText().toString());
        lang.setClan(clanET.getText().toString());
        lang.setFlag(flagET.getText().toString());
        
        apiService.updateLanguage(selectedLanguageId, lang).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<LanguageModel> call, @NonNull Response<LanguageModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Language Updated", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    clearLanguageFields();
                }
            }
            @Override public void onFailure(@NonNull Call<LanguageModel> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
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
        searchLanguageET.setText("");
    }

    private void createCourse() {
        String title = courseTitleET.getText().toString();
        String desc = courseDescET.getText().toString();
        String langName = langSpinnerForCourses.getText().toString();
        
        Long langId = null;
        for (LanguageModel l : languagesList) {
            if (l.getName().equals(langName)) {
                langId = l.getId();
                break;
            }
        }
        if (title.isEmpty() || langId == null) return;

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(desc);
        course.setLanguageId(langId);
        course.setModerationNote(courseModerationNoteET.getText().toString());
        course.setReviewedAt(courseReviewedAtET.getText().toString());
        
        apiService.createCourse(course).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Course> call, @NonNull Response<Course> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Course Created", Toast.LENGTH_SHORT).show();
                    loadDashboardStats();
                    clearCourseFields();
                }
            }
            @Override public void onFailure(@NonNull Call<Course> call, @NonNull Throwable t) {}
        });
    }

    private void updateCourse() {
        if (selectedCourseId == null) {
            Toast.makeText(this, "No course selected to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = courseTitleET.getText().toString();
        String desc = courseDescET.getText().toString();
        String langName = langSpinnerForCourses.getText().toString();

        Long langId = null;
        for (LanguageModel l : languagesList) {
            if (l.getName().equals(langName)) {
                langId = l.getId();
                break;
            }
        }
        if (title.isEmpty() || langId == null) return;

        Course course = new Course();
        course.setId(selectedCourseId);
        course.setTitle(title);
        course.setDescription(desc);
        course.setLanguageId(langId);
        course.setModerationNote(courseModerationNoteET.getText().toString());
        course.setReviewedAt(courseReviewedAtET.getText().toString());

        apiService.updateCourse(selectedCourseId, course).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Course> call, @NonNull Response<Course> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Course Updated", Toast.LENGTH_SHORT).show();
                    loadDashboardStats();
                    clearCourseFields();
                }
            }
            @Override public void onFailure(@NonNull Call<Course> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCourseFields() {
        selectedCourseId = null;
        courseTitleET.setText("");
        courseDescET.setText("");
        courseModerationNoteET.setText("");
        courseReviewedAtET.setText("");
        langSpinnerForCourses.setText("");
        searchCourseET.setText("");
    }

    private void createLesson() {
        String title = lessonTitleET.getText().toString();
        String content = lessonContentET.getText().toString();
        String courseTitle = courseSpinnerForLessons.getText().toString();
        
        Long courseId = null;
        for (Course c : coursesList) {
            if (c.getTitle().equals(courseTitle)) {
                courseId = c.getId();
                break;
            }
        }
        if (title.isEmpty() || courseId == null) return;

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setContent(content);
        lesson.setCourseId(courseId);
        lesson.setModerationNote(lessonModerationNoteET.getText().toString());
        lesson.setReviewedAt(lessonReviewedAtET.getText().toString());
        lesson.setSubmissionStatus(lessonStatusET.getText().toString());
        
        apiService.createLesson(lesson).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Lesson> call, @NonNull Response<Lesson> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Lesson Created", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    clearLessonFields();
                }
            }
            @Override public void onFailure(@NonNull Call<Lesson> call, @NonNull Throwable t) {}
        });
    }

    private void updateLesson() {
        if (selectedLessonId == null) {
            Toast.makeText(this, "No lesson selected to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = lessonTitleET.getText().toString();
        String content = lessonContentET.getText().toString();
        String courseTitle = courseSpinnerForLessons.getText().toString();

        Long courseId = null;
        for (Course c : coursesList) {
            if (c.getTitle().equals(courseTitle)) {
                courseId = c.getId();
                break;
            }
        }
        if (title.isEmpty() || courseId == null) return;

        Lesson lesson = new Lesson();
        lesson.setId(selectedLessonId);
        lesson.setTitle(title);
        lesson.setContent(content);
        lesson.setCourseId(courseId);
        lesson.setModerationNote(lessonModerationNoteET.getText().toString());
        lesson.setReviewedAt(lessonReviewedAtET.getText().toString());
        lesson.setSubmissionStatus(lessonStatusET.getText().toString());

        apiService.updateLesson(selectedLessonId, lesson).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Lesson> call, @NonNull Response<Lesson> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Lesson Updated", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    clearLessonFields();
                }
            }
            @Override public void onFailure(@NonNull Call<Lesson> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearLessonFields() {
        selectedLessonId = null;
        lessonTitleET.setText("");
        lessonContentET.setText("");
        lessonModerationNoteET.setText("");
        lessonReviewedAtET.setText("");
        lessonStatusET.setText("");
        courseSpinnerForLessons.setText("");
        searchLessonET.setText("");
    }

    private void createExercise() {
        String prompt = exercisePromptET.getText().toString();
        String questionText = exerciseQuestionTextET.getText().toString();
        String ans = exerciseAnsET.getText().toString();
        String options = exerciseOptionsET.getText().toString();
        String hint = exerciseHintET.getText().toString();
        String type = exerciseTypeSpinner.getText().toString();
        String lessonTitle = lessonSpinnerForExercises.getText().toString();
        
        Long lessonId = null;
        for (Lesson l : lessonsList) {
            if (l.getTitle().equals(lessonTitle)) {
                lessonId = l.getId();
                break;
            }
        }
        if (prompt.isEmpty() || lessonId == null) return;

        Question q = new Question();
        q.setQuestion(prompt);
        q.setQuestionText(questionText);
        q.setAnswer(ans);
        q.setOptions(options);
        q.setHint(hint);
        q.setType(type);
        q.setLessonId(lessonId);
        
        apiService.addQuestion(q).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Question> call, @NonNull Response<Question> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Exercise Created", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    clearExerciseFields();
                }
            }
            @Override public void onFailure(@NonNull Call<Question> call, @NonNull Throwable t) {}
        });
    }

    private void clearExerciseFields() {
        selectedExerciseId = null;
        exercisePromptET.setText("");
        exerciseQuestionTextET.setText("");
        exerciseAnsET.setText("");
        exerciseOptionsET.setText("");
        exerciseHintET.setText("");
        exerciseTypeSpinner.setText("");
    }

    private void updateExercise() {
        if (selectedExerciseId == null) {
            Toast.makeText(this, "No exercise selected to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String prompt = exercisePromptET.getText().toString();
        String questionText = exerciseQuestionTextET.getText().toString();
        String ans = exerciseAnsET.getText().toString();
        String options = exerciseOptionsET.getText().toString();
        String hint = exerciseHintET.getText().toString();
        String type = exerciseTypeSpinner.getText().toString();
        String lessonTitle = lessonSpinnerForExercises.getText().toString();

        Long lessonId = null;
        for (Lesson l : lessonsList) {
            if (l.getTitle().equals(lessonTitle)) {
                lessonId = l.getId();
                break;
            }
        }
        if (prompt.isEmpty() || lessonId == null) return;

        Question q = new Question();
        q.setId(selectedExerciseId);
        q.setQuestion(prompt);
        q.setQuestionText(questionText);
        q.setAnswer(ans);
        q.setOptions(options);
        q.setHint(hint);
        q.setType(type);
        q.setLessonId(lessonId);

        apiService.editExercise(selectedExerciseId, q).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Question> call, @NonNull Response<Question> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Exercise Updated", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                    clearExerciseFields();
                }
            }
            @Override public void onFailure(@NonNull Call<Question> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteExercise(Question item) {
        apiService.deleteExercise(item.getId()).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Exercise Deleted", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }

    private void deleteLesson(Lesson lesson) {
        apiService.deleteLesson(lesson.getId()).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Lesson Deleted", Toast.LENGTH_SHORT).show();
                    loadInitialData();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(AdminActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Vocabulary Management ---

    private void populateVocabFields(VocabularyItem item) {
        selectedVocabId = item.getId();
        vocabWordET.setText(item.getWord());
        vocabTargetET.setText(item.getWordTarget());
        vocabPhoneticET.setText(item.getPhonetic());
        vocabTranslationET.setText(item.getTranslation());
        vocabExampleET.setText(item.getExampleSentence());
        vocabTopicET.setText(item.getTopic());

        if (item.getLanguageId() != null) {
            for (LanguageModel l : languagesList) {
                if (l.getId().longValue() == item.getLanguageId().longValue()) {
                    String lName = l.getName() != null ? l.getName() : l.getLanguageName();
                    vocabLangSpinner.setText(lName, false);
                    updateCourseSpinnerForVocab(lName);
                    break;
                }
            }
        }

        if (item.getCourseId() != null) {
            for (Course c : coursesList) {
                if (c.getId().longValue() == item.getCourseId().longValue()) {
                    vocabCourseSpinner.setText(c.getTitle(), false);
                    updateLessonSpinnerForVocab(c.getTitle());
                    break;
                }
            }
        }

        if (item.getLessonId() != null) {
            for (Lesson l : lessonsList) {
                if (l.getId().longValue() == item.getLessonId().longValue()) {
                    vocabLessonSpinner.setText(l.getTitle(), false);
                    break;
                }
            }
        }
    }

    private void createVocab() {
        VocabularyItem v = getVocabFromFields();
        if (v == null) return;
        apiService.createVocabulary(v).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<VocabularyItem> call, @NonNull Response<VocabularyItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Vocab Created", Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                    clearVocabFields();
                }
            }
            @Override public void onFailure(@NonNull Call<VocabularyItem> call, @NonNull Throwable t) {}
        });
    }

    private void updateVocab() {
        if (selectedVocabId == null) return;
        VocabularyItem v = getVocabFromFields();
        if (v == null) return;
        apiService.updateVocabulary(selectedVocabId, v).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<VocabularyItem> call, @NonNull Response<VocabularyItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Vocab Updated", Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                    clearVocabFields();
                }
            }
            @Override public void onFailure(@NonNull Call<VocabularyItem> call, @NonNull Throwable t) {}
        });
    }

    private void deleteVocab(VocabularyItem item) {
        apiService.deleteVocabulary(item.getId()).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Vocab Deleted", Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }

    private VocabularyItem getVocabFromFields() {
        String word = vocabWordET.getText().toString();
        String target = vocabTargetET.getText().toString();
        String phonetic = vocabPhoneticET.getText().toString();
        String translation = vocabTranslationET.getText().toString();
        String example = vocabExampleET.getText().toString();
        String topic = vocabTopicET.getText().toString();
        String langName = vocabLangSpinner.getText().toString();
        String courseTitle = vocabCourseSpinner.getText().toString();
        String lessonTitle = vocabLessonSpinner.getText().toString();

        Long langId = null;
        for (LanguageModel l : languagesList) {
            String lName = l.getName() != null ? l.getName() : l.getLanguageName();
            if (lName != null && lName.equals(langName)) {
                langId = l.getId();
                break;
            }
        }

        Long courseId = null;
        for (Course c : coursesList) {
            if (c.getTitle() != null && c.getTitle().equals(courseTitle)) {
                courseId = c.getId();
                break;
            }
        }

        Long lessonId = null;
        for (Lesson l : lessonsList) {
            if (l.getTitle() != null && l.getTitle().equals(lessonTitle)) {
                lessonId = l.getId();
                break;
            }
        }

        if (word.isEmpty() || translation.isEmpty()) {
            Toast.makeText(this, "Word and translation required", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        if (langId == null || courseId == null || lessonId == null) {
            Toast.makeText(this, "Please select valid Language, Course, and Lesson", Toast.LENGTH_SHORT).show();
            return null;
        }

        VocabularyItem v = new VocabularyItem();
        v.setWord(word);
        v.setWordTarget(target);
        v.setPhonetic(phonetic);
        v.setTranslation(translation);
        v.setExampleSentence(example);
        v.setTopic(topic);
        v.setLanguageId(langId);
        v.setCourseId(courseId);
        v.setLessonId(lessonId);
        
        android.util.Log.d("AdminActivity", "Saving Vocab: " + word + " with LangID=" + langId + ", CourseID=" + courseId + ", LessonID=" + lessonId);

        return v;
    }

    private void clearVocabFields() {
        selectedVocabId = null;
        vocabWordET.setText("");
        vocabTargetET.setText("");
        vocabPhoneticET.setText("");
        vocabTranslationET.setText("");
        vocabExampleET.setText("");
        vocabTopicET.setText("");
        vocabLangSpinner.setText("");
        vocabCourseSpinner.setText("");
        vocabLessonSpinner.setText("");
    }

    private void deleteUser(User user) {
        apiService.deleteUser(user.getId()).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "User Deleted", Toast.LENGTH_SHORT).show();
                    loadDashboardStats();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }

    private void promoteUser(User user) {
        user.setRole("ADMIN");
        apiService.editUser(user.getId(), user).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "User Promoted", Toast.LENGTH_SHORT).show();
                    loadDashboardStats();
                }
            }
            @Override public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
        });
    }

    // Exercise Manage Adapter
    private static class ExerciseManageAdapter extends RecyclerView.Adapter<ExerciseManageAdapter.ViewHolder> {
        private final List<Question> items;
        private final List<Lesson> lessons;
        private final OnExerciseActionListener listener;

        interface OnExerciseActionListener { void onEdit(Question item); void onDelete(Question item); }

        ExerciseManageAdapter(List<Question> items, List<Lesson> lessons, OnExerciseActionListener listener) {
            this.items = items;
            this.lessons = lessons;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_exercise, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Question item = items.get(position);
            holder.prompt.setText(item.getPrompt());
            holder.type.setText(item.getType());
            
            String lessonTitle = "No Lesson";
            if (item.getLessonId() != null) {
                for (Lesson l : lessons) {
                    if (l.getId().equals(item.getLessonId())) {
                        lessonTitle = l.getTitle();
                        break;
                    }
                }
            }
            holder.lesson.setText(lessonTitle);

            // Separate by lessons: Show header if it's the first item or lesson changed
            if (position == 0 || !items.get(position - 1).getLessonId().equals(item.getLessonId())) {
                holder.lessonHeader.setVisibility(View.VISIBLE);
                holder.lessonHeader.setText("LESSON: " + lessonTitle.toUpperCase());
            } else {
                holder.lessonHeader.setVisibility(View.GONE);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView prompt, type, lesson, lessonHeader;
            ImageButton btnEdit, btnDelete;
            ViewHolder(View v) {
                super(v);
                prompt = v.findViewById(R.id.manageExercisePrompt);
                type = v.findViewById(R.id.manageExerciseType);
                lesson = v.findViewById(R.id.manageExerciseLesson);
                lessonHeader = v.findViewById(R.id.exerciseLessonHeader);
                btnEdit = v.findViewById(R.id.btnEditExercise);
                btnDelete = v.findViewById(R.id.btnDeleteExercise);
            }
        }
    }

    // Lesson Manage Adapter
    private static class LessonManageAdapter extends RecyclerView.Adapter<LessonManageAdapter.ViewHolder> {
        private final List<Lesson> items;
        private final OnLessonActionListener listener;

        interface OnLessonActionListener { void onEdit(Lesson item); void onDelete(Lesson item); }

        LessonManageAdapter(List<Lesson> items, OnLessonActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_lesson, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Lesson item = items.get(position);
            holder.title.setText(item.getTitle());
            holder.status.setText(item.getSubmissionStatus());
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, status;
            ImageButton btnEdit, btnDelete;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.manageLessonTitle);
                status = v.findViewById(R.id.manageLessonStatus);
                btnEdit = v.findViewById(R.id.btnEditLesson);
                btnDelete = v.findViewById(R.id.btnDeleteLesson);
            }
        }
    }

    // Language Detail Adapter
    private static class LanguageDetailAdapter extends RecyclerView.Adapter<LanguageDetailAdapter.ViewHolder> {
        private final List<LanguageModel> items;
        private final OnLanguageActionListener listener;

        interface OnLanguageActionListener { void onManage(LanguageModel item); }

        LanguageDetailAdapter(List<LanguageModel> items, OnLanguageActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_detail, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LanguageModel item = items.get(position);
            holder.name.setText(item.getName());
            holder.province.setText(item.getProvince() != null ? item.getProvince() : "N/A");
            holder.district.setText(item.getDistrict() != null ? item.getDistrict() : "N/A");
            holder.clan.setText(item.getClan() != null ? item.getClan() : "N/A");
            
            holder.lessonCount.setText(String.valueOf(item.getLessonCount()));
            holder.exerciseCount.setText(String.valueOf(item.getExerciseCount()));
            holder.vocabCount.setText(String.valueOf(item.getVocabCount()));
            holder.audioCoverage.setText(item.getAudioCoverage() + "%");

            // Flag Loading with local fallback
            if (item.getName() != null && item.getName().equalsIgnoreCase("Motu")) {
                holder.flag.setImageResource(R.drawable.central_flag);
            } else if (item.getName() != null && item.getName().equalsIgnoreCase("Tok Pisin")) {
                holder.flag.setImageResource(R.drawable.png_flag);
            } else if (item.getFlag() != null && !item.getFlag().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getFlag())
                        .placeholder(R.drawable.rualingo_logo)
                        .transform(new CircleCrop())
                        .into(holder.flag);
            } else {
                holder.flag.setImageResource(R.drawable.rualingo_logo);
            }

            holder.btnManage.setOnClickListener(v -> listener.onManage(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, province, district, clan, lessonCount, exerciseCount, vocabCount, audioCoverage;
            ImageView flag;
            MaterialButton btnManage;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.lblLanguageName);
                province = v.findViewById(R.id.lblProvinceName);
                district = v.findViewById(R.id.lblDistrictName);
                clan = v.findViewById(R.id.lblClanName);
                lessonCount = v.findViewById(R.id.lblLessonCount);
                exerciseCount = v.findViewById(R.id.lblExerciseCount);
                vocabCount = v.findViewById(R.id.lblVocabCount);
                audioCoverage = v.findViewById(R.id.lblAudioCoverage);
                flag = v.findViewById(R.id.imgLanguageFlag);
                btnManage = v.findViewById(R.id.btnManageLanguage);
            }
        }
    }

    // Course Detail Adapter
    private static class CourseDetailAdapter extends RecyclerView.Adapter<CourseDetailAdapter.ViewHolder> {
        private final List<Course> items;
        private final OnCourseActionListener listener;

        interface OnCourseActionListener { void onManage(Course item); }

        CourseDetailAdapter(List<Course> items, OnCourseActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_detail, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Course item = items.get(position);
            holder.title.setText(item.getTitle());
            holder.language.setText(item.getLanguageName() != null ? item.getLanguageName() : "Unknown Language");
            holder.description.setText(item.getDescription() != null ? item.getDescription() : "No description available.");
            holder.moderationNote.setText(item.getModerationNote() != null ? item.getModerationNote() : "N/A");
            holder.reviewedAt.setText(item.getReviewedAt() != null ? item.getReviewedAt() : "N/A");

            if (item.getLanguageName() != null && item.getLanguageName().equalsIgnoreCase("Motu")) {
                holder.icon.setImageResource(R.drawable.central_flag);
            } else if (item.getLanguageName() != null && item.getLanguageName().equalsIgnoreCase("Tok Pisin")) {
                holder.icon.setImageResource(R.drawable.png_flag);
            } else {
                holder.icon.setImageResource(R.drawable.rualingo_logo);
            }

            holder.btnManage.setOnClickListener(v -> listener.onManage(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, language, description, moderationNote, reviewedAt;
            ImageView icon;
            MaterialButton btnManage;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.lblCourseTitle);
                language = v.findViewById(R.id.lblCourseLanguage);
                description = v.findViewById(R.id.lblCourseDescription);
                moderationNote = v.findViewById(R.id.lblCourseModerationNote);
                reviewedAt = v.findViewById(R.id.lblCourseReviewedAt);
                icon = v.findViewById(R.id.imgCourseIcon);
                btnManage = v.findViewById(R.id.btnManageCourse);
            }
        }
    }

    // Generic Adapter for Management Lists
    private static class GenericAdminAdapter<T> extends RecyclerView.Adapter<GenericAdminAdapter.ViewHolder> {
        private final List<T> items;
        private final ItemLabelProvider<T> labelProvider;
        private final OnItemClickListener<T> clickListener;

        interface ItemLabelProvider<T> { String getLabel(T item); }
        interface OnItemClickListener<T> { void onItemClick(T item); }

        GenericAdminAdapter(List<T> items, ItemLabelProvider<T> labelProvider, OnItemClickListener<T> clickListener) {
            this.items = items;
            this.labelProvider = labelProvider;
            this.clickListener = clickListener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            T item = items.get(position);
            holder.text.setText(labelProvider.getLabel(item));
            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            ViewHolder(View v) { super(v); text = v.findViewById(android.R.id.text1); }
        }
    }

    private static class VocabularyManageAdapter extends RecyclerView.Adapter<VocabularyManageAdapter.ViewHolder> {
        private final List<VocabularyItem> items;
        private final List<LanguageModel> languages;
        private final List<Course> courses;
        private final List<Lesson> lessons;
        private final OnVocabActionListener listener;

        interface OnVocabActionListener { void onEdit(VocabularyItem item); void onDelete(VocabularyItem item); }

        VocabularyManageAdapter(List<VocabularyItem> items, List<LanguageModel> languages, List<Course> courses, List<Lesson> lessons, OnVocabActionListener listener) {
            this.items = items;
            this.languages = languages;
            this.courses = courses;
            this.lessons = lessons;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_vocabulary, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VocabularyItem item = items.get(position);
            holder.word.setText(item.getWord() != null ? item.getWord() : "No Word");
            holder.translation.setText(item.getTranslation() != null ? item.getTranslation() : "No Translation");
            
            Long itemLangId = item.getLanguageId();
            Long itemCourseId = item.getCourseId();
            Long itemLessonId = item.getLessonId();
            
            String langName = null;
            if (itemLangId != null) {
                for (LanguageModel l : languages) {
                    if (l.getId() != null && l.getId().longValue() == itemLangId.longValue()) {
                        langName = l.getName() != null ? l.getName() : l.getLanguageName();
                        break;
                    }
                }
            }
            
            String courseTitle = null;
            if (itemCourseId != null) {
                for (Course c : courses) {
                    if (c.getId() != null && c.getId().longValue() == itemCourseId.longValue()) {
                        courseTitle = c.getTitle();
                        break;
                    }
                }
            }

            String lessonTitle = null;
            if (itemLessonId != null) {
                for (Lesson l : lessons) {
                    if (l.getId() != null && l.getId().longValue() == itemLessonId.longValue()) {
                        lessonTitle = l.getTitle();
                        break;
                    }
                }
            }
            
            // Fallback: If ID is null, try to match Lesson Name by the "Topic" field
            if (lessonTitle == null && item.getTopic() != null) {
                for (Lesson l : lessons) {
                    if (l.getTitle() != null && l.getTitle().equalsIgnoreCase(item.getTopic())) {
                        lessonTitle = l.getTitle();
                        break;
                    }
                }
            }

            // High Priority Fallback: Names directly in object
            if (langName == null && item.getLanguageName() != null) langName = item.getLanguageName();
            if (courseTitle == null && item.getCourseTitle() != null) courseTitle = item.getCourseTitle();
            if (lessonTitle == null && item.getLessonTitle() != null) lessonTitle = item.getLessonTitle();

            String displayLang = (langName != null ? langName : "Unknown");
            String displayCourse = (courseTitle != null ? courseTitle : "Unknown");
            String displayLesson = (lessonTitle != null ? lessonTitle : "Unknown");
            
            // For sub-info only, keep IDs for debugging
            String infoLesson = displayLesson + " (ID:" + (itemLessonId != null ? itemLessonId : "NULL") + ")";
            holder.info.setText(String.format("%s | %s | Topic: %s", displayCourse, infoLesson, item.getTopic() != null ? item.getTopic() : "General"));

            // Header logic: Show if first or if language/course/lesson changed
            String currentHeaderText = String.format("%s - %s - %s", displayLang.toUpperCase(), displayCourse.toUpperCase(), displayLesson.toUpperCase());
            
            boolean showHeader = false;
            if (position == 0) {
                showHeader = true;
            } else {
                // Determine previous header text to see if we need a new one
                VocabularyItem prev = items.get(position - 1);
                String prevLang = "Unknown";
                if (prev.getLanguageId() != null) {
                    for (LanguageModel l : languages) {
                        if (l.getId() != null && l.getId().longValue() == prev.getLanguageId().longValue()) {
                            prevLang = l.getName() != null ? l.getName() : l.getLanguageName();
                            break;
                        }
                    }
                }
                if (prevLang.equals("Unknown") && prev.getLanguageName() != null) prevLang = prev.getLanguageName();

                String prevCourse = "Unknown";
                if (prev.getCourseId() != null) {
                    for (Course c : courses) {
                        if (c.getId() != null && c.getId().longValue() == prev.getCourseId().longValue()) {
                            prevCourse = c.getTitle();
                            break;
                        }
                    }
                }
                if (prevCourse.equals("Unknown") && prev.getCourseTitle() != null) prevCourse = prev.getCourseTitle();

                String prevLesson = "Unknown";
                if (prev.getLessonId() != null) {
                    for (Lesson l : lessons) {
                        if (l.getId() != null && l.getId().longValue() == prev.getLessonId().longValue()) {
                            prevLesson = l.getTitle();
                            break;
                        }
                    }
                } else if (prev.getTopic() != null) {
                    for (Lesson l : lessons) {
                        if (l.getTitle() != null && l.getTitle().equalsIgnoreCase(prev.getTopic())) {
                            prevLesson = l.getTitle();
                            break;
                        }
                    }
                }
                if (prevLesson.equals("Unknown") && prev.getLessonTitle() != null) prevLesson = prev.getLessonTitle();

                String prevHeaderText = String.format("%s - %s - %s", prevLang.toUpperCase(), prevCourse.toUpperCase(), prevLesson.toUpperCase());
                if (!currentHeaderText.equals(prevHeaderText)) {
                    showHeader = true;
                }
            }

            if (showHeader) {
                holder.header.setVisibility(View.VISIBLE);
                holder.header.setText(currentHeaderText);
            } else {
                holder.header.setVisibility(View.GONE);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView word, translation, info, header;
            ImageButton btnEdit, btnDelete;
            ViewHolder(View v) {
                super(v);
                word = v.findViewById(R.id.manageVocabWord);
                translation = v.findViewById(R.id.manageVocabTranslation);
                info = v.findViewById(R.id.manageVocabInfo);
                header = v.findViewById(R.id.vocabHeader);
                btnEdit = v.findViewById(R.id.btnEditVocab);
                btnDelete = v.findViewById(R.id.btnDeleteVocab);
            }
        }
    }
}
