package com.app.rualingoapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int totalExercises = 0;
    private String selectedOption = null;
    private View selectedOptionView = null;

    // Phases: FLASHCARDS -> QUIZ
    private boolean isFlashcardPhase = true;
    private boolean isStoryPhase = false;
    private int flashcardIndex = 0;
    private final List<Question> flashcardList = new ArrayList<>();
    private String lessonContent;

    private ProgressBar progressBar;
    private TextView questionPrompt;
    private TextView questionText;
    private LinearLayout optionsContainer;
    private MaterialButton checkButton;
    private View feedbackOverlay, characterSection, flashcardView;
    private TextView feedbackTitle, flashcardWord, flashcardTranslation, flashcardPhonetic, flashcardExample, flashcardTopic, flashcardEnglishName;
    private MaterialButton flashcardAudioBtn;
    private TextView correctAnswerText, hintText;
    private MaterialButton continueButton;
    
    // New Content Type Views
    private View textContentView, matchingGrid;
    private TextView textContentText;
    private MaterialButton micButton;
    private MediaPlayer mediaPlayer;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        questions = (List<Question>) getIntent().getSerializableExtra("questions");
        lessonContent = getIntent().getStringExtra("lessonContent");

        // DUOLINGO-STYLE CONNECTION: 
        // Filter the incoming list into Flashcards (vocabulary) and Exercises (quizzes)
        if (questions != null) {
            List<Question> actualExercises = new ArrayList<>();
            for (Question q : questions) {
                if ("vocabulary".equalsIgnoreCase(q.getType())) {
                    flashcardList.add(q);
                } else {
                    actualExercises.add(q);
                }
            }
            // Replace the full list with just the drills so displayQuestion() works correctly
            questions = actualExercises;
            totalExercises = questions.size();
        }

        progressBar = findViewById(R.id.progressBar);
        questionPrompt = findViewById(R.id.questionPrompt);
        questionText = findViewById(R.id.questionText);
        optionsContainer = findViewById(R.id.optionsContainer);
        checkButton = findViewById(R.id.checkButton);
        feedbackOverlay = findViewById(R.id.feedbackOverlay);
        feedbackTitle = findViewById(R.id.feedbackTitle);
        correctAnswerText = findViewById(R.id.correctAnswerText);
        continueButton = findViewById(R.id.continueButton);
        
        characterSection = findViewById(R.id.characterSection);
        flashcardView = findViewById(R.id.flashcardView);
        flashcardWord = findViewById(R.id.flashcardWord);
        flashcardEnglishName = findViewById(R.id.flashcardEnglishName);
        flashcardTranslation = findViewById(R.id.flashcardTranslation);
        flashcardPhonetic = findViewById(R.id.flashcardPhonetic);
        flashcardExample = findViewById(R.id.flashcardExample);
        flashcardTopic = findViewById(R.id.flashcardTopic);
        flashcardAudioBtn = findViewById(R.id.flashcardAudioBtn);
        hintText = findViewById(R.id.hintText);

        // New Views
        textContentView = findViewById(R.id.textContentView);
        textContentText = findViewById(R.id.textContentText);
        matchingGrid = findViewById(R.id.matchingGrid);
        micButton = findViewById(R.id.micButton);

        findViewById(R.id.closeButton).setOnClickListener(v -> finish());

        displayNext();

        checkButton.setOnClickListener(v -> {
            if (isFlashcardPhase) {
                flashcardIndex++;
                displayNext();
            } else if (isStoryPhase) {
                finishLesson();
            } else {
                checkAnswer();
            }
        });
        continueButton.setOnClickListener(v -> nextQuestion());

        micButton.setOnClickListener(v -> {
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
            v.setEnabled(false);
            v.postDelayed(() -> {
                checkButton.setEnabled(true);
                v.setEnabled(true);
            }, 2000);
        });
    }

    private void displayNext() {
        if (isFlashcardPhase && flashcardIndex < flashcardList.size()) {
            showFlashcard();
        } else {
            isFlashcardPhase = false;
            displayQuestion();
        }
    }

    private void showFlashcard() {
        Question q = flashcardList.get(flashcardIndex);
        android.util.Log.d("QuestionActivity", "Rendering Flashcard: Native=" + q.getQuestion() + ", English=" + q.getQuestionText() + ", Meaning=" + q.getAnswer());
        
        characterSection.setVisibility(View.GONE);
        flashcardView.setVisibility(View.VISIBLE);
        optionsContainer.setVisibility(View.GONE);
        textContentView.setVisibility(View.GONE);
        matchingGrid.setVisibility(View.GONE);
        micButton.setVisibility(View.GONE);

        questionPrompt.setText("New Word Found!");
        questionPrompt.setTextColor(ContextCompat.getColor(this, R.color.duo_blue));
        
        // Display the vocabulary word and its details
        String nativeWord = q.getQuestion(); // v.getWordTarget()
        String englishName = q.getQuestionText(); // v.getWord()
        String meaning = q.getAnswer(); // v.getTranslation()

        // 1. Native Word (Target) - Large & Bold
        if (nativeWord != null && !nativeWord.isEmpty()) {
            flashcardWord.setText(nativeWord);
            flashcardWord.setVisibility(View.VISIBLE);
        } else if (englishName != null && !englishName.isEmpty()) {
            // Fallback: Use English if Native is missing
            flashcardWord.setText(englishName);
            flashcardWord.setVisibility(View.VISIBLE);
        }

        // 2. English Name
        if (englishName != null && !englishName.isEmpty() && !englishName.equals(nativeWord)) {
            flashcardEnglishName.setText(englishName);
            flashcardEnglishName.setVisibility(View.VISIBLE);
        } else {
            flashcardEnglishName.setVisibility(View.GONE);
        }

        // 3. Detailed Meaning / Translation
        if (meaning != null && !meaning.isEmpty()) {
            flashcardTranslation.setText(meaning);
            flashcardTranslation.setVisibility(View.VISIBLE);
        } else {
            flashcardTranslation.setVisibility(View.GONE);
        }

        // 4. Topic/Unit Header
        String topic = q.getTopic() != null ? q.getTopic() : "Unit 1";
        flashcardTopic.setText(topic);

        // 5. Phonetic Guide
        if (q.getPhonetic() != null && !q.getPhonetic().isEmpty()) {
            flashcardPhonetic.setText("[" + q.getPhonetic() + "]");
            flashcardPhonetic.setVisibility(View.VISIBLE);
        } else {
            flashcardPhonetic.setVisibility(View.GONE);
        }

        // 6. Example Sentence
        if (q.getExampleSentence() != null && !q.getExampleSentence().isEmpty()) {
            flashcardExample.setText("Example: " + q.getExampleSentence());
            flashcardExample.setVisibility(View.VISIBLE);
        } else {
            flashcardExample.setVisibility(View.GONE);
        }

        // 7. Audio Play Button
        flashcardAudioBtn.setOnClickListener(v -> {
            if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                playAudio(q.getAudioPath());
            }
        });

        // Auto-play on appear
        if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
            playAudio(q.getAudioPath());
        }

        checkButton.setText("GOT IT!");
        checkButton.setEnabled(true);
        checkButton.setBackgroundResource(R.drawable.btn_3d_blue_selectable);
        checkButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.duo_blue_dark)));
        feedbackOverlay.setVisibility(View.GONE);
        
        // Progress reflects teaching phase
        progressBar.setProgress((int)(((float)flashcardIndex / flashcardList.size()) * 20));
    }

    private void displayQuestion() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            showLessonStory();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        android.util.Log.d("QuestionActivity", "Loading Question: " + q.getPrompt() + " (Type: " + q.getType() + ")");
        
        // Renderer Logic: Choose screen based on subType
        String subType = q.getSubType() != null ? q.getSubType() : (q.getType() != null ? q.getType() : "multiple_choice");

        // Default visibility reset
        characterSection.setVisibility(View.VISIBLE);
        flashcardView.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.GONE);
        textContentView.setVisibility(View.GONE);
        matchingGrid.setVisibility(View.GONE);
        micButton.setVisibility(View.GONE);
        feedbackOverlay.setVisibility(View.GONE);
        hintText.setVisibility(View.GONE);

        questionPrompt.setTextColor(Color.parseColor("#3C3C3C"));
        
        String instruction = q.getQuestion(); // e.g. "Translate this sentence"
        String mainContent = q.getQuestionText(); // e.g. "Niu"

        // Logic to swap if fields are used differently in some languages
        if (mainContent == null || mainContent.isEmpty() || mainContent.toLowerCase().contains("translate")) {
            if (instruction != null && !instruction.toLowerCase().contains("translate")) {
                // Swap: instruction has the word, mainContent has the command
                String temp = mainContent;
                mainContent = instruction;
                instruction = temp;
            }
        }

        // Final fallbacks for the bubble text
        if (mainContent == null || mainContent.isEmpty()) {
            mainContent = q.getCorrectAnswer();
        }

        questionText.setText(mainContent);
        
        // Final fallbacks for the top instruction text
        if (instruction == null || instruction.isEmpty() || instruction.length() < 3) {
            instruction = getString(R.string.translate_sentence);
        }
        questionPrompt.setText(instruction);
        
        // Show Hint if available
        if (q.getHint() != null && !q.getHint().isEmpty()) {
            hintText.setText("Hint: " + q.getHint());
            hintText.setVisibility(View.VISIBLE);
        }
        
        checkButton.setEnabled(false);
        checkButton.setText(R.string.check_btn);

        switch (subType) {
            case "dialogue" -> { // Story Screen: chat bubbles
                characterSection.setVisibility(View.GONE);
                textContentView.setVisibility(View.VISIBLE);
                questionPrompt.setText(instruction);
                textContentText.setText(mainContent);
                checkButton.setText(R.string.got_it);
                checkButton.setEnabled(true);
            }
            case "audio_quiz" -> { // Audio Screen: Speaker icon
                micButton.setVisibility(View.VISIBLE);
                questionPrompt.setText("What did you hear?");
                questionText.setText("Tap to play audio");
                micButton.setOnClickListener(v -> {
                    playAudio(q.getAudioPath());
                    checkButton.setEnabled(true);
                });
            }
            case "match" -> { // Match Screen: Two columns
                matchingGrid.setVisibility(View.VISIBLE);
                characterSection.setVisibility(View.GONE);
                questionPrompt.setText("Match the pairs");
                setupMatchingExercise(q);
            }
            case "rule_card" -> { // Grammar Lesson
                characterSection.setVisibility(View.GONE);
                textContentView.setVisibility(View.VISIBLE);
                questionPrompt.setText(instruction);
                textContentText.setText(mainContent);
                checkButton.setText(R.string.got_it);
                checkButton.setEnabled(true);
            }
            default -> { // fill_blank, multiple_choice, translation
                optionsContainer.setVisibility(View.VISIBLE);
                if (subType.equals("fill_blank")) {
                    questionPrompt.setText("Complete the sentence");
                }
                setupOptionsExercise(q);
            }
        }

        int progress = 30 + (int) (((float) (currentQuestionIndex) / Math.max(1, totalExercises)) * 70);
        progressBar.setProgress(progress);
    }

    private void setupOptionsExercise(Question q) {
        optionsContainer.removeAllViews();
        selectedOption = null;
        selectedOptionView = null;
        
        List<String> options = q.getOptionsList();
        String correctAnswer = q.getCorrectAnswer();
        if (correctAnswer != null && !correctAnswer.trim().isEmpty()) {
            boolean found = false;
            for (String opt : options) {
                if (opt.equalsIgnoreCase(correctAnswer.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                options.add(correctAnswer.trim());
            }
        }
        Collections.shuffle(options);

        for (String option : options) {
            String cleanOption = option.trim();
            if (cleanOption.isEmpty()) continue;
            
            MaterialButton optionBtn = (MaterialButton) getLayoutInflater().inflate(R.layout.item_option_button, optionsContainer, false);
            optionBtn.setText(cleanOption);
            
            optionBtn.setOnClickListener(v -> {
                if (selectedOptionView != null) {
                    selectedOptionView.setSelected(false);
                }
                
                v.setSelected(true);
                
                selectedOptionView = v;
                selectedOption = cleanOption;
                checkButton.setEnabled(true);

                String prompt = q.getPrompt();
                if (prompt != null && (prompt.contains("[ ]") || prompt.contains("___"))) {
                    String filled = prompt.replace("[ ]", "[ " + cleanOption + " ]")
                                          .replace("___", cleanOption);
                    questionText.setText(filled);
                }
            });

            optionsContainer.addView(optionBtn);
        }
    }

    private void setupMatchingExercise(Question q) {
        checkButton.setEnabled(true);
        checkButton.setText("MATCHING COMPLETED (DEMO)");
    }

    private void checkAnswer() {
        Question q = questions.get(currentQuestionIndex);
        String subType = q.getSubType() != null ? q.getSubType() : (q.getType() != null ? q.getType() : "multiple_choice");
        
        boolean isCorrectResult = false;
        if (subType.equals("dialogue") || subType.equals("rule_card")) {
            isCorrectResult = true;
        } else if (subType.equals("multiple_choice") || subType.equals("fill_blank") || subType.equals("translation") || subType.equals("audio_quiz")) {
            String correctAnswer = q.getCorrectAnswer();
            if (correctAnswer != null && selectedOption != null) {
                isCorrectResult = selectedOption.equalsIgnoreCase(correctAnswer.trim());
            }
        }

        // Haptic Feedback for Duolingo feel
        View view = getWindow().getDecorView();
        if (isCorrectResult) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        } else {
            // Two quick pulses for error
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        }

        // --- NEW: Sync response to server for lesson completion and vocabulary unlocking ---
        Long userId = new SessionManager(this).getUserId();
        if (userId != -1 && q.getId() != null) {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("exerciseId", q.getId());
            payload.put("answer", selectedOption != null ? selectedOption : "viewed");
            payload.put("isCorrect", isCorrectResult);
            payload.put("attempts", 1);

            RetrofitClient.getApiService().saveUserResponse(userId, payload).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                    android.util.Log.d("QuestionActivity", "Sync response success: " + response.code());
                }
                @Override public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                    android.util.Log.e("QuestionActivity", "Sync response failed", t);
                }
            });
        }
        // ----------------------------------------------------------------------------------

        feedbackOverlay.setVisibility(View.VISIBLE);
        if (isCorrectResult) {
            correctCount++;
            feedbackOverlay.setBackgroundColor(Color.parseColor("#D7FFB8")); // Soft Green
            feedbackTitle.setText(R.string.great_job_caps);
            feedbackTitle.setTextColor(Color.parseColor("#58A700"));
            correctAnswerText.setVisibility(View.GONE);
            continueButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#58CC02")));
            continueButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#46A302")));
            continueButton.setTextColor(Color.WHITE);
        } else {
            questions.add(q);
            feedbackOverlay.setBackgroundColor(Color.parseColor("#FFDFE0")); // Soft Red
            feedbackTitle.setText(R.string.incorrect_caps);
            feedbackTitle.setTextColor(Color.parseColor("#CE1126"));
            
            String answerDisplay = q.getCorrectAnswer();
            android.util.Log.d("QuestionActivity", "Check Result: INCORRECT. Question ID: " + q.getId() + ", Correct Answer: " + answerDisplay);
            
            if (answerDisplay != null && !answerDisplay.trim().isEmpty() && !answerDisplay.equalsIgnoreCase("null")) {
                correctAnswerText.setVisibility(View.VISIBLE);
                correctAnswerText.setText(getString(R.string.correct_answer_format, answerDisplay));
            } else {
                // If the answer is genuinely missing from the model, we hide the label entirely
                // to prevent "Correct answer: null"
                correctAnswerText.setVisibility(View.GONE);
                android.util.Log.e("QuestionActivity", "DATA ERROR: Question #" + q.getId() + " has no valid correct answer field.");
            }

            correctAnswerText.setTextColor(Color.parseColor("#CE1126"));
            continueButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CE1126")));
            continueButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#A00E1E")));
            continueButton.setTextColor(Color.WHITE);
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            showLessonStory();
        }
    }

    private void showLessonStory() {
        if (isStoryPhase || lessonContent == null || lessonContent.trim().isEmpty()) {
            finishLesson();
            return;
        }

        isStoryPhase = true;
        isFlashcardPhase = false;
        characterSection.setVisibility(View.GONE);
        flashcardView.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.GONE);
        matchingGrid.setVisibility(View.GONE);
        micButton.setVisibility(View.GONE);
        feedbackOverlay.setVisibility(View.GONE);
        textContentView.setVisibility(View.VISIBLE);
        questionPrompt.setText("Lesson story");
        textContentText.setText(lessonContent);
        checkButton.setText("FINISH LESSON");
        checkButton.setEnabled(true);
        progressBar.setProgress(100);
    }

    private void finishLesson() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Intent intent = new Intent(this, LessonCompleteActivity.class);
        intent.putExtra("score", correctCount);
        intent.putExtra("total", questions.size());
        if (!questions.isEmpty()) {
            intent.putExtra("lessonId", questions.get(0).getLessonId());
        }
        startActivity(intent);
        finish();
    }

    private void playAudio(String audioPath) {
        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(this, "No audio path provided", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            if (audioPath.startsWith("http")) {
                mediaPlayer.setDataSource(audioPath.trim());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            } else {
                // Strip extension, handle spaces/multiple underscores, and lowercase
                // e.g., "good _morning.m4a" -> "good_morning"
                String resourceName = audioPath.trim().toLowerCase()
                        .replace(" ", "_")
                        .replaceAll("_+", "_");

                if (resourceName.contains(".")) {
                    resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
                }

                int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(this, resId);
                    mediaPlayer.start();
                } else {
                    Toast.makeText(this, "Audio file not found: " + resourceName, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
