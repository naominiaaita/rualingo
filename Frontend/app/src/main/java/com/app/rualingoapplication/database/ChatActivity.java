package com.app.rualingoapplication.database;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.rualingoapplication.ApiService;
import com.app.rualingoapplication.ChatMessage;
import com.app.rualingoapplication.R;
import com.app.rualingoapplication.RetrofitClient;
import com.app.rualingoapplication.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnChatClickListener {

    private static final int SPEECH_REQUEST_CODE = 100;
    private EditText messageInput;
    private ApiService chatApi;
    private ChatAdapter chatAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private TextToSpeech tts;
    private SessionManager sessionManager;
    private ProgressBar lessonProgressBar;
    private TextView streakText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Initialize UI components
        messageInput = findViewById(R.id.message_input);
        ImageButton sendButton = findViewById(R.id.send_button);
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);
        ImageButton backButton = findViewById(R.id.backButton);
        lessonProgressBar = findViewById(R.id.lessonProgressBar);
        streakText = findViewById(R.id.streakText);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 2. Initialize Retrofit
        RetrofitClient.setContext(this);
        chatApi = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);

        // Update UI with session data
        if (streakText != null) {
            streakText.setText(String.valueOf(sessionManager.getStreak()));
        }

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });

        // Voice Input
        ImageButton voiceButton = findViewById(R.id.voice_button);
        if (voiceButton != null) {
            voiceButton.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                startVoiceRecognition();
            });
        }

        // 3. Set the Click Listener
        sendButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                updateChat(text, true);
                sendQueryToRua(text);
                messageInput.setText(""); // Clear input after sending
            }
        });

        // 4. Trigger initial greeting
        sendQueryToRua("");
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Rua...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                messageInput.setText(result.get(0));
            }
        }
    }

    @Override
    public void onSpeakClick(String text) {
        if (tts != null) {
            // Clean the text before speaking
            String cleanText = text.replaceFirst("(?i)Rua says:\\s*", "");
            // Remove HTML tags
            cleanText = cleanText.replaceAll("<[^>]*>", "");
            
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void sendQueryToRua(String userText) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        // Backend ChatTutorRequestDTO (ask-v2) expects the 'message' field, not 'userQuery'
        payload.put("message", userText);

        long lessonId = sessionManager.getCurrentLessonId();
        if (lessonId != -1) {
            payload.put("lessonId", lessonId);
        }

        chatApi.askRua(payload).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessage> call, @NonNull Response<ChatMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String ruaReply = response.body().getResponse();
                    
                    // Personalize initial greeting if it's the first message (userText is empty)
                    if (userText.isEmpty() && ruaReply != null && ruaReply.toLowerCase().contains("hi learner")) {
                        String name = sessionManager.getFirstName();
                        if (name == null || name.trim().isEmpty()) {
                            name = sessionManager.getUsername();
                        }
                        
                        if (name != null && !name.equalsIgnoreCase("Learner")) {
                            ruaReply = ruaReply.replaceFirst("(?i)Hi learner", "Hi " + name);
                        }
                    }

                    updateChat(ruaReply, false);
                    
                    // Gamification: XP Toast Simulation
                    if (userText.length() > 5) {
                        showXpToast();
                    }
                } else if (response.code() != 401) {
                    Toast.makeText(ChatActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessage> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, "Rua is offline!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChat(String message, boolean isUser) {
        if (message == null || message.trim().isEmpty()) return;

        if (isUser) {
            messageList.add(new Message(message, true));
        } else {
            // Split AI response by double line breaks
            String[] blocks = message.split("\\n\\n+");
            for (String block : blocks) {
                String trimmedBlock = block.trim();
                if (trimmedBlock.isEmpty()) continue;

                // Detect if the block contains help options to split them into separate bubbles
                if (trimmedBlock.contains("I can help you with:") || 
                    trimmedBlock.contains("Pronunciation") || 
                    trimmedBlock.contains("Vocabulary")) {
                    
                    String[] lines = trimmedBlock.split("\\n");
                    for (String line : lines) {
                        String trimmedLine = line.trim();
                        if (trimmedLine.isEmpty()) continue;
                        
                        // Process the line for bolding and bracket removal
                        String processed = processHelpItem(trimmedLine);
                        messageList.add(new Message(processed, false));
                    }
                } else {
                    messageList.add(new Message(trimmedBlock, false));
                }
            }
            
            // Progress bar animation mock
            if (lessonProgressBar != null) {
                int currentProgress = lessonProgressBar.getProgress();
                lessonProgressBar.setProgress(Math.min(100, currentProgress + 10), true);
            }
        }
        
        chatAdapter.notifyDataSetChanged();
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);
        if (chatRecyclerView != null) {
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }
    }

    private String processHelpItem(String item) {
        // Matches pattern: [Emoji] [Title] (Ask: [Description])
        // Example: 🔊 Pronunciation (Ask: 'How do I say...')
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^([\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|\\u2705|\\uD83D\\uDCE2|\\uD83D\\uDCD6|\\uD83C\\uDF93)\\s+([^\\(]+)\\s*\\(([^\\)]+)\\)$");
        java.util.regex.Matcher matcher = pattern.matcher(item);
        
        if (matcher.find()) {
            String emoji = matcher.group(1).trim();
            String title = matcher.group(2).trim();
            String instruction = matcher.group(3).trim();
            // Remove brackets and bold the title
            return emoji + " <b>" + title + "</b> " + instruction;
        }
        return item;
    }

    private void showXpToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_xp_toast, null);
        TextView view = layout.findViewById(R.id.toastText);
        if (view != null) view.setText("+10 XP");
        
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(android.view.Gravity.CENTER, 0, 0);
        toast.show();
        
        // Actually add XP to session
        sessionManager.addXP(10);
    }
}
