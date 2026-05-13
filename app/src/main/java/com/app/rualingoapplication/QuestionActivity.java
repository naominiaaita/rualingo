package com.app.rualingoapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
    private int flashcardIndex = 0;
    private final List<Question> flashcardList = new ArrayList<>();

    private LinearProgressIndicator progressBar;
    private TextView questionPrompt;
    private TextView questionText;
    private LinearLayout optionsContainer;
    private MaterialButton checkButton;
    private View feedbackOverlay, characterSection, flashcardView;
    private TextView feedbackTitle, flashcardWord, flashcardTranslation;
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
        flashcardTranslation = findViewById(R.id.flashcardTranslation);
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
        
        characterSection.setVisibility(View.GONE);
        flashcardView.setVisibility(View.VISIBLE);
        optionsContainer.setVisibility(View.GONE);
        textContentView.setVisibility(View.GONE);
        matchingGrid.setVisibility(View.GONE);
        micButton.setVisibility(View.GONE);

        questionPrompt.setText("New Word Found!");
        questionPrompt.setTextColor(ContextCompat.getColor(this, R.color.duo_blue));
        
        // Display the vocabulary word and its details
        flashcardWord.setText(q.getQuestion()); // The target word
        flashcardTranslation.setText(q.getAnswer()); // The translation

        checkButton.setText("GOT IT!");
        checkButton.setEnabled(true);
        checkButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.duo_blue)));
        checkButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.duo_blue_dark)));
        feedbackOverlay.setVisibility(View.GONE);
        
        // Progress reflects teaching phase
        progressBar.setProgress((int)(((float)flashcardIndex / flashcardList.size()) * 20));
    }

    private void displayQuestion() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            finishLesson();
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
        
        // Smarter Prompt Logic: 
        // 1. Prefer QuestionText (e.g. "Hello") for the bubble if available.
        // 2. Use Question as fallback or if QuestionText is empty.
        // 3. Avoid showing generic instructions like "Translate this word" as the main bubble text.
        
        String displayMain = q.getQuestionText();
        String displaySecondary = q.getQuestion();
        String translation = q.getCorrectAnswer();

        if (displayMain == null || displayMain.isEmpty() || displayMain.equalsIgnoreCase("Translate this word")) {
            displayMain = (displaySecondary != null && !displaySecondary.equalsIgnoreCase("Translate this word")) ? displaySecondary : translation;
        }

        if (subType.equals("translation") || subType.equals("multiple_choice")) {
            questionText.setText(displayMain);
        } else {
            questionText.setText(displayMain);
        }
        
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
                questionPrompt.setText(q.getQuestion() != null ? q.getQuestion() : "Story Dialogue");
                // Parse dialogue lines from metadata if available
                String content = q.getQuestionText();
                if (q.getMetadata() != null && q.getMetadata().contains("lines")) {
                    content = "Story metadata rendering active..."; // Placeholder
                }
                textContentText.setText(content);
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
                questionPrompt.setText(q.getQuestion() != null ? q.getQuestion() : "Grammar Rule");
                textContentText.setText(q.getQuestionText());
                checkButton.setText(R.string.got_it);
                checkButton.setEnabled(true);
            }
            default -> { // fill_blank, multiple_choice, translation
                optionsContainer.setVisibility(View.VISIBLE);
                if (subType.equals("fill_blank")) {
                    questionPrompt.setText("Complete the sentence");
                } else if (subType.equals("translation")) {
                    questionPrompt.setText(R.string.translate_sentence);
                    // For translation, ensure we show the target word clearly
                    if (q.getQuestion() != null && !q.getQuestion().isEmpty()) {
                        questionText.setText(q.getQuestion());
                    }
                } else {
                    questionPrompt.setText(R.string.translate_sentence);
                }
                setupOptionsExercise(q);
            }
        }

        int progress = 30 + (int) (((float) (correctCount) / Math.max(1, totalExercises)) * 70);
        progressBar.setProgress(progress);
    }

    private void setupOptionsExercise(Question q) {
        optionsContainer.removeAllViews();
        selectedOption = null;
        selectedOptionView = null;
        
        List<String> options = q.getOptionsList();
        
        // Ensure correct answer is in the options list
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
        
        // Shuffle to avoid predictable patterns
        Collections.shuffle(options);

        android.util.Log.d("QuestionActivity", "Rendering " + (options != null ? options.size() : 0) + " options for prompt: " + q.getPrompt());
        
        if (options != null) {
            for (String option : options) {
                String cleanOption = option.trim();
                if (cleanOption.isEmpty()) continue;
                
                com.google.android.material.button.MaterialButton optionBtn = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int marginPx = (int) (8 * getResources().getDisplayMetrics().density);
                params.setMargins(0, 0, 0, marginPx);
                optionBtn.setLayoutParams(params);
                
                optionBtn.setText(cleanOption);
                optionBtn.setAllCaps(false);
                optionBtn.setTextColor(Color.parseColor("#4B4B4B"));
                optionBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5E5E5")));
                optionBtn.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
                optionBtn.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
                int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
                optionBtn.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                optionBtn.setMinHeight((int) (60 * getResources().getDisplayMetrics().density));
                
                optionBtn.setOnClickListener(v -> {
                    if (selectedOptionView != null) {
                        ((com.google.android.material.button.MaterialButton)selectedOptionView).setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5E5E5")));
                        ((com.google.android.material.button.MaterialButton)selectedOptionView).setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                    }
                    
                    optionBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.duo_blue)));
                    optionBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E5F3FF")));
                    
                    selectedOptionView = optionBtn;
                    selectedOption = cleanOption;
                    checkButton.setEnabled(true);

                    // Update the question text to fill the blank if it exists
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
    }

    private void setupMatchingExercise(Question q) {
        // Dynamic matching UI would be complex for a single surgical edit, 
        // for now we just show them all and auto-enable check
        checkButton.setEnabled(true);
        checkButton.setText("MATCHING COMPLETED (DEMO)");
    }

    private void checkAnswer() {
        Question q = questions.get(currentQuestionIndex);
        String subType = q.getSubType() != null ? q.getSubType() : (q.getType() != null ? q.getType() : "multiple_choice");
        
        boolean isCorrectResult = false;
        if (subType.equals("dialogue") || subType.equals("rule_card")) {
            isCorrectResult = true;
        } else if (subType.equals("multiple_choice") || subType.equals("fill_blank") || subType.equals("translation")) {
            String correctAnswer = q.getCorrectAnswer();
            if (correctAnswer != null && selectedOption != null) {
                isCorrectResult = selectedOption.equalsIgnoreCase(correctAnswer.trim());
            }
        } else if (subType.equals("audio_quiz")) {
            String correctAnswer = q.getCorrectAnswer();
            if (correctAnswer != null && selectedOption != null) {
                isCorrectResult = selectedOption.equalsIgnoreCase(correctAnswer.trim());
            }
        }

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
            // Add wrong question to the end to try again later (Duolingo style)
            questions.add(q);

            feedbackOverlay.setBackgroundColor(Color.parseColor("#FFDFE0")); // Soft Red
            feedbackTitle.setText(R.string.incorrect_caps);
            feedbackTitle.setTextColor(Color.parseColor("#CE1126")); // PNG Red
            correctAnswerText.setVisibility(View.VISIBLE);
            correctAnswerText.setText(getString(R.string.correct_answer_format, q.getCorrectAnswer()));
            correctAnswerText.setTextColor(Color.parseColor("#CE1126"));
            continueButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CE1126"))); // PNG Red
            continueButton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#A00E1E")));
            continueButton.setTextColor(Color.WHITE);
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            finishLesson();
        }
    }

    private void finishLesson() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Intent intent = new Intent(this, LessonCompleteActivity.class);
        intent.putExtra("score", correctCount);
        intent.putExtra("total", questions.size());
        startActivity(intent);
        finish();
    }

    private void playAudio(String audioPath) {
        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(this, "No audio path provided", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            mediaPlayer = new MediaPlayer();
            if (audioPath.startsWith("http")) {
                mediaPlayer.setDataSource(audioPath);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            } else {
                // Try to find resource by name in res/raw
                int resId = getResources().getIdentifier(audioPath, "raw", getPackageName());
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(this, resId);
                    mediaPlayer.start();
                } else {
                    Toast.makeText(this, "Audio file not found: " + audioPath, Toast.LENGTH_SHORT).show();
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
