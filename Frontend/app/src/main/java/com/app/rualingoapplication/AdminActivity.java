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
import com.google.android.material.tabs.TabLayout;
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
    private android.widget.ProgressBar adminProgressBar;
    private View btnLogout;
    private View overviewSection, languagesSection, coursesSection, exercisesSection, vocabularySection;
    private View coursesSubSection, lessonsSubSection;
    private BottomNavigationView bottomNavigation;
    private EditText etSearchUsers;
    private RecyclerView rvUserRoster;
    
    // UI Elements for Import
    private MaterialButton importLessonsButton;
    private static final int PICK_JSON_FILE = 2;
    
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
        
        importLessonsButton = findViewById(R.id.importLessonsButton);
        if (importLessonsButton != null) {
            importLessonsButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/json");
                startActivityForResult(intent, PICK_JSON_FILE);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileInfo();
        loadInitialData();
    }

    private void refreshProfileInfo() {
        if (tvAdminUsername != null) tvAdminUsername.setText(sessionManager.getUsername());
        if (tvAdminRole != null) tvAdminRole.setText(sessionManager.getRole() + " • System");
        
        String pic = sessionManager.getProfilePicture();
        if (pic != null && !pic.isEmpty()) {
            ImageView img = findViewById(R.id.imgAdminAvatar);
            if (img != null) {
                Glide.with(this).load(pic).transform(new CircleCrop()).into(img);
            }
        }
    }

    private void bindViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvTotalLearners = findViewById(R.id.tvTotalLearners);
        tvSyncQueue = findViewById(R.id.tvSyncQueue);
        tvAdminUsername = findViewById(R.id.tvAdminUsername);
        tvAdminRole = findViewById(R.id.tvAdminRole);
        adminProgressBar = findViewById(R.id.adminProgressBar);
        btnLogout = findViewById(R.id.btnLogout);

        overviewSection = findViewById(R.id.overviewSection);
        languagesSection = findViewById(R.id.languagesSection);
        coursesSection = findViewById(R.id.coursesSection);
        exercisesSection = findViewById(R.id.exercisesSection);
        vocabularySection = findViewById(R.id.vocabularySection);
        
        coursesSubSection = findViewById(R.id.coursesSubSection);
        lessonsSubSection = findViewById(R.id.lessonsSubSection);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        etSearchUsers = findViewById(R.id.etSearchUsers);
        rvUserRoster = findViewById(R.id.rvUserRoster);
        rvUserRoster.setLayoutManager(new LinearLayoutManager(this));

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
        lessonTopicET = findViewById(R.id.lessonTopicEditText);
        lessonDescriptionET = findViewById(R.id.lessonDescriptionEditText);
        lessonContentET = findViewById(R.id.lessonDescEditText);
        lessonModerationNoteET = findViewById(R.id.lessonModerationNoteEditText);
        lessonStatusET = findViewById(R.id.lessonStatusEditText);
        lessonReviewedAtET = findViewById(R.id.lessonReviewedAtEditText);

        exercisePromptET = findViewById(R.id.exerciseQuestionEditText);
        exerciseQuestionTextET = findViewById(R.id.exerciseQuestionTextEditText);
        exerciseAnsET = findViewById(R.id.correctAnsEditText);
        exerciseOptionsET = findViewById(R.id.optionsEditText);
        exerciseHintET = findViewById(R.id.exerciseHintEditText);

        vocabWordET = findViewById(R.id.vocabWordEditText);
        vocabTargetET = findViewById(R.id.vocabTargetEditText);
        vocabPhoneticET = findViewById(R.id.vocabPhoneticEditText);
        vocabTranslationET = findViewById(R.id.vocabTranslationEditText);

        // Spinners
        languageSpinnerForCourses = findViewById(R.id.languageSpinnerForCourses);
        courseSpinnerForLessons = findViewById(R.id.courseSpinnerForLessons);
        lessonSpinnerForExercises = findViewById(R.id.lessonSpinnerForExercises);
        exerciseTypeSpinner = findViewById(R.id.exerciseTypeSpinner);
        vocabLessonSpinner = findViewById(R.id.vocabLessonSpinner);

        setupLogout();
        setupCrudActions();
        setupCascadingSpinners();
        setupDatePickers();
        setupTabs();
        
        RecyclerView rvLangs = findViewById(R.id.languagesRecyclerView);
        rvLangs.setLayoutManager(new LinearLayoutManager(this));
        languagesAdapter = new LanguageDetailAdapter(languagesList, this::populateLanguageFields);
        rvLangs.setAdapter(languagesAdapter);

        RecyclerView rvCourses = findViewById(R.id.coursesRecyclerView);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(new CourseDetailAdapter(coursesList, this::populateCourseFields));

        RecyclerView rvLessons = findViewById(R.id.lessonsRecyclerView);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(new LessonManageAdapter(lessonsList, new LessonManageAdapter.OnLessonActionListener() {
            @Override public void onEdit(Lesson item) { populateLessonFields(item); }
            @Override public void onDelete(Lesson item) { deleteLesson(item); }
        }));

        RecyclerView rvExercises = findViewById(R.id.exercisesRecyclerView);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(new QuestionManageAdapter(exercisesList, new QuestionManageAdapter.OnQuestionActionListener() {
            @Override public void onEdit(Question item) { populateExerciseFields(item); }
            @Override public void onDelete(Question item) { deleteExercise(item); }
        }));

        RecyclerView rvVocab = findViewById(R.id.vocabRecyclerView);
        rvVocab.setLayoutManager(new LinearLayoutManager(this));
        vocabAdapter = new VocabularyManageAdapter(vocabularyList, languagesList, coursesList, lessonsList, new VocabularyManageAdapter.OnVocabActionListener() {
            @Override public void onEdit(VocabularyItem item) { populateVocabFields(item); }
            @Override public void onDelete(VocabularyItem item) { deleteVocab(item); }
        });
        rvVocab.setAdapter(vocabAdapter);
    }

    private void setupTabs() {
        TabLayout tabs = findViewById(R.id.coursesSubTabLayout);
        if (tabs != null) {
            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        coursesSubSection.setVisibility(View.VISIBLE);
                        lessonsSubSection.setVisibility(View.GONE);
                    } else {
                        coursesSubSection.setVisibility(View.GONE);
                        lessonsSubSection.setVisibility(View.VISIBLE);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupBottomNav() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_overview) switchSection("overview");
            else if (id == R.id.admin_languages) switchSection("languages");
            else if (id == R.id.admin_lessons) switchSection("lessons");
            else if (id == R.id.admin_exercises) switchSection("exercises");
            else if (id == R.id.admin_vocabulary) switchSection("vocabulary");
            return true;
        });
    }

    private void switchSection(String section) {
        overviewSection.setVisibility("overview".equals(section) ? View.VISIBLE : View.GONE);
        languagesSection.setVisibility("languages".equals(section) ? View.VISIBLE : View.GONE);
        coursesSection.setVisibility("lessons".equals(section) ? View.VISIBLE : View.GONE);
        exercisesSection.setVisibility("exercises".equals(section) ? View.VISIBLE : View.GONE);
        vocabularySection.setVisibility("vocabulary".equals(section) ? View.VISIBLE : View.GONE);

        String title = "Admin Center";
        switch (section) {
            case "languages": title = "Manage Languages"; break;
            case "lessons": title = "Course Management"; break;
            case "exercises": title = "Curriculum Exercises"; break;
            case "vocabulary": title = "Vocabulary Bank"; break;
        }
        tvHeaderTitle.setText(title);
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupCrudActions() {
        findViewById(R.id.createLanguageButton).setOnClickListener(v -> createLanguage());
        findViewById(R.id.updateLanguageButton).setOnClickListener(v -> updateLanguage());
        findViewById(R.id.deleteLanguageButton).setOnClickListener(v -> deleteLanguage());

        findViewById(R.id.createCourseButton).setOnClickListener(v -> createCourse());
        findViewById(R.id.updateCourseButton).setOnClickListener(v -> updateCourse());
        findViewById(R.id.deleteCourseButton).setOnClickListener(v -> deleteCourse());

        findViewById(R.id.createLessonButton).setOnClickListener(v -> createLesson());
        findViewById(R.id.updateLessonButton).setOnClickListener(v -> updateLesson());

        findViewById(R.id.createExerciseButton).setOnClickListener(v -> createExercise());
        findViewById(R.id.updateExerciseButton).setOnClickListener(v -> updateExercise());

        findViewById(R.id.createVocabButton).setOnClickListener(v -> createVocab());
        findViewById(R.id.updateVocabButton).setOnClickListener(v -> updateVocab());
    }

    private void setupCascadingSpinners() {
        languageSpinnerForCourses.setOnItemClickListener((parent, view, position, id) -> {
            LanguageModel lm = (LanguageModel) parent.getItemAtPosition(position);
            selectedLanguageId = lm.getId();
        });
        
        courseSpinnerForLessons.setOnItemClickListener((parent, view, position, id) -> {
            Course c = (Course) parent.getItemAtPosition(position);
            selectedCourseId = c.getId();
        });
    }

    private void setupDatePickers() {
        courseReviewedAtET.setOnClickListener(v -> showDatePicker(courseReviewedAtET));
        lessonReviewedAtET.setOnClickListener(v -> showDatePicker(lessonReviewedAtET));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            et.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void populateLanguageFields(LanguageModel lm) {
        selectedLanguageId = lm.getId();
        languageNameET.setText(lm.getName());
        provinceET.setText(lm.getProvince());
        districtET.setText(lm.getDistrict());
        clanET.setText(lm.getClan());
        flagET.setText(lm.getFlag());
        sourceET.setText(lm.getSource());
    }

    private void populateCourseFields(Course c) {
        selectedCourseId = c.getId();
        courseTitleET.setText(c.getTitle());
        courseDescET.setText(c.getDescription());
        courseModerationNoteET.setText(c.getModerationNote());
        courseFlagET.setText(c.getFlag());
        courseReviewedAtET.setText(c.getReviewedAt() != null ? c.getReviewedAt().toString() : "");
    }

    private void populateLessonFields(Lesson l) {
        selectedLessonId = l.getId();
        lessonTitleET.setText(l.getTitle());
        lessonTopicET.setText(l.getTopic());
        lessonDescriptionET.setText(l.getDescription());
        lessonContentET.setText(l.getContent());
        lessonModerationNoteET.setText(l.getModerationNote());
        lessonStatusET.setText(l.getSubmissionStatus());
        lessonReviewedAtET.setText(l.getReviewedAt() != null ? l.getReviewedAt().toString() : "");
    }

    private void loadDashboardStats() {
        apiService.fetchAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalLearners.setText(String.valueOf(response.body().size()));
                    updateModerationLists(response.body());
                }
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {}
        });
    }

    private void loadInitialData() {
        loadDashboardStats();
        
        apiService.getLanguages().enqueue(new Callback<List<LanguageModel>>() {
            @Override
            public void onResponse(Call<List<LanguageModel>> call, Response<List<LanguageModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateLanguageList(response.body());
                }
            }
            @Override public void onFailure(Call<List<LanguageModel>> call, Throwable t) {}
        });

        apiService.getCourses().enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCourseList(response.body());
                }
            }
            @Override public void onFailure(Call<List<Course>> call, Throwable t) {}
        });

        apiService.getLessons(null).enqueue(new Callback<List<Lesson>>() {
            @Override
            public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateLessonList(response.body());
                }
            }
            @Override public void onFailure(Call<List<Lesson>> call, Throwable t) {}
        });

        apiService.getExercises(null).enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateExerciseList(response.body());
                }
            }
            @Override public void onFailure(Call<List<Question>> call, Throwable t) {}
        });
        
        loadVocabulary();
    }

    private void loadVocabulary() {
        apiService.getVocabulary(null, null, null).enqueue(new Callback<List<VocabularyItem>>() {
            @Override
            public void onResponse(Call<List<VocabularyItem>> call, Response<List<VocabularyItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vocabularyList.clear();
                    vocabularyList.addAll(response.body());
                    vocabAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<VocabularyItem>> call, Throwable t) {}
        });
    }

    private void updateLanguageList(List<LanguageModel> data) {
        languagesList.clear();
        languagesList.addAll(data);
        languagesAdapter.notifyDataSetChanged();
        updateLanguageSpinner();
    }

    private void updateCourseList(List<Course> data) {
        coursesList.clear();
        coursesList.addAll(data);
        updateCourseSpinner();
    }

    private void updateLessonList(List<Lesson> data) {
        lessonsList.clear();
        lessonsList.addAll(data);
    }

    private void updateExerciseList(List<Question> data) {
        exercisesList.clear();
        exercisesList.addAll(data);
    }

    private void updateLanguageSpinner() {
        ArrayAdapter<LanguageModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, languagesList);
        languageSpinnerForCourses.setAdapter(adapter);
    }

    private void updateCourseSpinner() {
        ArrayAdapter<Course> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, coursesList);
        courseSpinnerForLessons.setAdapter(adapter);
    }

    private void updateModerationLists(List<User> users) {
        rvUserRoster.setAdapter(new UserModerationAdapter(users, new UserModerationAdapter.OnUserActionListener() {
            @Override public void onDeleteClick(User item) { deleteUser(item); }
            @Override public void onPromoteClick(User item) { promoteUser(item); }
            @Override public void onHistoryClick(User item) { viewUserHistory(item); }
        }));
    }

    private void createLanguage() {
        LanguageModel lm = new LanguageModel();
        lm.setName(languageNameET.getText().toString());
        lm.setProvince(provinceET.getText().toString());
        lm.setDistrict(districtET.getText().toString());
        lm.setClan(clanET.getText().toString());
        lm.setFlag(flagET.getText().toString());
        lm.setSource(sourceET.getText().toString());
        apiService.createLanguage(lm).enqueue(new Callback<LanguageModel>() {
            @Override public void onResponse(Call<LanguageModel> call, Response<LanguageModel> response) {
                if (response.isSuccessful()) { Toast.makeText(AdminActivity.this, "Created", Toast.LENGTH_SHORT).show(); loadInitialData(); }
            }
            @Override public void onFailure(Call<LanguageModel> call, Throwable t) {}
        });
    }

    private void updateLanguage() {
        if (selectedLanguageId == null) return;
        LanguageModel lm = new LanguageModel();
        lm.setName(languageNameET.getText().toString());
        lm.setProvince(provinceET.getText().toString());
        lm.setDistrict(districtET.getText().toString());
        lm.setClan(clanET.getText().toString());
        lm.setFlag(flagET.getText().toString());
        lm.setSource(sourceET.getText().toString());
        apiService.updateLanguage(selectedLanguageId, lm).enqueue(new Callback<LanguageModel>() {
            @Override public void onResponse(Call<LanguageModel> call, Response<LanguageModel> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<LanguageModel> call, Throwable t) {}
        });
    }

    private void deleteLanguage() {
        if (selectedLanguageId == null) return;
        apiService.deleteLanguage(selectedLanguageId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void createCourse() {
        Course c = new Course();
        c.setTitle(courseTitleET.getText().toString());
        c.setDescription(courseDescET.getText().toString());
        c.setLanguageId(selectedLanguageId);
        apiService.createCourse(c).enqueue(new Callback<Course>() {
            @Override public void onResponse(Call<Course> call, Response<Course> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Course> call, Throwable t) {}
        });
    }

    private void updateCourse() {
        if (selectedCourseId == null) return;
        Course c = new Course();
        c.setTitle(courseTitleET.getText().toString());
        c.setDescription(courseDescET.getText().toString());
        c.setLanguageId(selectedLanguageId);
        apiService.updateCourse(selectedCourseId, c).enqueue(new Callback<Course>() {
            @Override public void onResponse(Call<Course> call, Response<Course> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Course> call, Throwable t) {}
        });
    }

    private void deleteCourse() {
        if (selectedCourseId == null) return;
        apiService.deleteCourse(selectedCourseId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void createLesson() {
        Lesson l = new Lesson();
        l.setTitle(lessonTitleET.getText().toString());
        l.setCourseId(selectedCourseId);
        apiService.createLesson(l).enqueue(new Callback<Lesson>() {
            @Override public void onResponse(Call<Lesson> call, Response<Lesson> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Lesson> call, Throwable t) {}
        });
    }

    private void updateLesson() {
        if (selectedLessonId == null) return;
        Lesson l = new Lesson();
        l.setTitle(lessonTitleET.getText().toString());
        apiService.updateLesson(selectedLessonId, l).enqueue(new Callback<Lesson>() {
            @Override public void onResponse(Call<Lesson> call, Response<Lesson> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Lesson> call, Throwable t) {}
        });
    }

    private void deleteLesson(Lesson l) {
        apiService.deleteLesson(l.getId()).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void populateExerciseFields(Question q) {
        selectedExerciseId = q.getId();
        exercisePromptET.setText(q.getQuestion());
        exerciseQuestionTextET.setText(q.getQuestionText());
        exerciseAnsET.setText(q.getCorrectAnswer());
    }

    private void createExercise() {
        Question q = new Question();
        q.setQuestion(exercisePromptET.getText().toString());
        q.setCorrectAnswer(exerciseAnsET.getText().toString());
        apiService.addQuestion(q).enqueue(new Callback<Question>() {
            @Override public void onResponse(Call<Question> call, Response<Question> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Question> call, Throwable t) {}
        });
    }

    private void updateExercise() {
        if (selectedExerciseId == null) return;
        Question q = new Question();
        q.setQuestion(exercisePromptET.getText().toString());
        apiService.editExercise(selectedExerciseId, q).enqueue(new Callback<Question>() {
            @Override public void onResponse(Call<Question> call, Response<Question> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Question> call, Throwable t) {}
        });
    }

    private void deleteExercise(Question q) {
        apiService.deleteExercise(q.getId()).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void populateVocabFields(VocabularyItem vi) {
        selectedVocabId = vi.getId();
        vocabWordET.setText(vi.getWord());
    }

    private void createVocab() {
        VocabularyItem vi = new VocabularyItem();
        vi.setWord(vocabWordET.getText().toString());
        apiService.createVocabulary(vi).enqueue(new Callback<VocabularyItem>() {
            @Override public void onResponse(Call<VocabularyItem> call, Response<VocabularyItem> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<VocabularyItem> call, Throwable t) {}
        });
    }

    private void updateVocab() {
        if (selectedVocabId == null) return;
        VocabularyItem vi = new VocabularyItem();
        vi.setWord(vocabWordET.getText().toString());
        apiService.updateVocabulary(selectedVocabId, vi).enqueue(new Callback<VocabularyItem>() {
            @Override public void onResponse(Call<VocabularyItem> call, Response<VocabularyItem> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<VocabularyItem> call, Throwable t) {}
        });
    }

    private void deleteVocab(VocabularyItem vi) {
        apiService.deleteVocabulary(vi.getId()).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadInitialData();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void deleteUser(User user) {
        apiService.deleteUser(user.getId()).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadDashboardStats();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void promoteUser(User user) {
        apiService.editUser(user.getId(), user).enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) loadDashboardStats();
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void viewUserHistory(User user) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("userId", (long) user.getId());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadJson(data.getData());
        }
    }

    private void uploadJson(android.net.Uri uri) {
        try {
            android.os.ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) return;
            java.io.FileInputStream fis = new java.io.FileInputStream(pfd.getFileDescriptor());
            int size = (int) pfd.getStatSize();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String json = new String(buffer, "UTF-8");

            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> dataList = gson.fromJson(json, listType);

            if (adminProgressBar != null) adminProgressBar.setVisibility(View.VISIBLE);
            apiService.importLessons(dataList).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (adminProgressBar != null) adminProgressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminActivity.this, "Import successful", Toast.LENGTH_SHORT).show();
                        loadInitialData();
                    } else {
                        Toast.makeText(AdminActivity.this, "Import failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (adminProgressBar != null) adminProgressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("AdminActivity", "Error uploading JSON", e);
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }
}
