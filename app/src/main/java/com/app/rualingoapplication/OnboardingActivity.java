package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private int step = 0;
    private TextView onboardingText;
    private TextView questionTitle;
    private LinearLayout optionsContainer;
    private MaterialButton btnContinue;
    private View speechBubble;
    private String selectedLanguage;

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

        btnContinue.setOnClickListener(v -> nextStep());
        
        showStep();
    }

    private void nextStep() {
        step++;
        showStep();
    }

    private void showStep() {
        optionsContainer.removeAllViews();
        btnContinue.setVisibility(View.VISIBLE);

        switch (step) {
            case 0 -> { // Intro
                onboardingText.setText(R.string.hi_i_am_tree_kangaroo);
                questionTitle.setVisibility(View.GONE);
            }
            case 1 -> // Excited
                    onboardingText.setText(R.string.excited_to_meet);
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
                speechBubble.setVisibility(View.VISIBLE);
                onboardingText.setText(R.string.achieve_title);
                questionTitle.setVisibility(View.VISIBLE);
                questionTitle.setText(R.string.achieve_desc);
                btnContinue.setText(R.string.awesome);
            }
            case 6 -> { // Learning goal
                btnContinue.setText(R.string.continue_btn);
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
        btnContinue.setVisibility(View.GONE); // Hide until selection

        for (String opt : options) {
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            btn.setLayoutParams(params);
            btn.setText(opt);
            btn.setAllCaps(false);
            btn.setCornerRadius(32);
            btn.setOnClickListener(v -> nextStep());
            optionsContainer.addView(btn);
        }
    }
}
