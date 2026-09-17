package com.app.rualingoapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private int step = 0;
    private final int totalSteps = 8;
    private TextView onboardingText;
    private TextView questionTitle;
    private LinearLayout optionsContainer;
    private MaterialButton btnContinue;
    private View speechBubble;
    private ImageView mascotImage;
    private ProgressBar progressBar;
    private String selectedLanguage;
    private View selectedOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        SessionManager sessionManager = new SessionManager(this);
        selectedLanguage = sessionManager.getSelectedLanguage();

        onboardingText = findViewById(R.id.onboardingText);
        questionTitle = findViewById(R.id.questionTitle);
        optionsContainer = findViewById(R.id.onboardingOptionsContainer);
        btnContinue = findViewById(R.id.btnContinue);
        speechBubble = findViewById(R.id.speechBubble);
        mascotImage = findViewById(R.id.treeKangarooImage);
        progressBar = findViewById(R.id.onboardingProgress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnContinue.setOnClickListener(v -> nextStep());
        
        showStep();
    }

    private void nextStep() {
        step++;
        showStep();
    }

    private void showStep() {
        optionsContainer.removeAllViews();
        selectedOption = null;
        btnContinue.setEnabled(true);
        btnContinue.setText(R.string.continue_btn);
        
        // Reset layout params to default for mascot
        ConstraintLayout.LayoutParams mascotParams = (ConstraintLayout.LayoutParams) mascotImage.getLayoutParams();
        mascotParams.width = (int) (96 * getResources().getDisplayMetrics().density);
        mascotParams.height = (int) (96 * getResources().getDisplayMetrics().density);
        mascotImage.setLayoutParams(mascotParams);

        // Update Progress
        int progress = (int) (((float) (step + 1) / totalSteps) * 100);
        progressBar.setProgress(progress);

        switch (step) {
            case 0 -> { // Intro
                speechBubble.setVisibility(View.VISIBLE);
                onboardingText.setText(R.string.hi_i_am_tree_kangaroo);
                questionTitle.setVisibility(View.GONE);
            }
            case 1 -> { // Excited
                speechBubble.setVisibility(View.VISIBLE);
                onboardingText.setText(R.string.excited_to_meet);
                questionTitle.setVisibility(View.GONE);
            }
            case 2 -> // How heard
                    setupQuestion(getString(R.string.q_heard_about_us), new String[]{
                            getString(R.string.opt_social_media),
                            getString(R.string.opt_friends),
                            getString(R.string.opt_news),
                            getString(R.string.opt_other)
                    });
            case 3 -> // Why learning
                    setupQuestion(getString(R.string.q_why_learning, selectedLanguage), new String[]{
                            getString(R.string.opt_travel),
                            getString(R.string.opt_culture),
                            getString(R.string.opt_brain),
                            getString(R.string.opt_school)
                    });
            case 4 -> // How much know
                    setupQuestion(getString(R.string.q_how_much_know, selectedLanguage), new String[]{
                            getString(R.string.opt_new),
                            getString(R.string.opt_basics),
                            getString(R.string.opt_intermediate)
                    });
            case 5 -> { // Achieve
                // Layout adjustment for achievement screen
                mascotParams.width = (int) (140 * getResources().getDisplayMetrics().density);
                mascotParams.height = (int) (140 * getResources().getDisplayMetrics().density);
                mascotImage.setLayoutParams(mascotParams);

                speechBubble.setVisibility(View.VISIBLE);
                questionTitle.setVisibility(View.GONE); // Hide top bubble title
                
                // motivational text inside the speech bubble
                String boldText = "You'll build a <b><font color='#3B82F6'>500+ word vocabulary</font></b> and be able to navigate everyday situations.";
                onboardingText.setText(Html.fromHtml(boldText, Html.FROM_HTML_MODE_LEGACY));
                onboardingText.setTextSize(20); // Removed 'sp' as it's not valid Java
                
                btnContinue.setText(R.string.awesome);
            }
            case 6 -> { // Learning goal
                onboardingText.setTextSize(17);
                setupQuestion(getString(R.string.q_learning_goal), new String[]{
                        getString(R.string.goal_casual),
                        getString(R.string.goal_regular),
                        getString(R.string.goal_serious),
                        getString(R.string.goal_insane)
                });
            }
            case 7 -> // Where to start
                    setupQuestion(getString(R.string.q_where_to_start), new String[]{
                            getString(R.string.start_scratch),
                            getString(R.string.start_placement)
                    });
            case 8 -> { // Lead to Home course
                SessionManager sm = new SessionManager(this);
                sm.setNewUser(false); // No longer a new user after finishing onboarding
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
            default -> {
            }
        }
    }

    private void setupQuestion(String title, String[] options) {
        speechBubble.setVisibility(View.GONE);
        questionTitle.setVisibility(View.VISIBLE);
        questionTitle.setText(title);
        
        btnContinue.setEnabled(false); 
        btnContinue.setAlpha(0.5f);

        for (String opt : options) {
            MaterialButton btn = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_onboarding_option, optionsContainer, false);
            btn.setText(opt);
            
            btn.setOnClickListener(v -> {
                if (selectedOption != null) {
                    selectedOption.setSelected(false);
                }
                v.setSelected(true);
                selectedOption = v;
                btnContinue.setEnabled(true);
                btnContinue.setAlpha(1.0f);
                
                v.invalidate();
            });
            
            optionsContainer.addView(btn);
        }
    }
}
