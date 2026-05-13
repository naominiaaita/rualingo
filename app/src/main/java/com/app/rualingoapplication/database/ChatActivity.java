package com.app.rualingoapplication.database;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rualingoapplication.ApiService;
import com.app.rualingoapplication.ChatMessage;
import com.app.rualingoapplication.R;
import com.app.rualingoapplication.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ApiService chatApi;
    private ChatAdapter chatAdapter;
    private final List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Initialize UI components
        messageInput = findViewById(R.id.message_input);
        ImageButton sendButton = findViewById(R.id.send_button);
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 2. Initialize Retrofit
        RetrofitClient.setContext(this);
        chatApi = RetrofitClient.getApiService();

        // 3. Set the Click Listener
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                updateChat(text, true);
                sendQueryToRua(text);
                messageInput.setText(""); // Clear input after sending
            }
        });
    }

    private void sendQueryToRua(String userText) {
        ChatMessage request = new ChatMessage();
        request.setUserQuery(userText);

        chatApi.askRua(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessage> call, @NonNull Response<ChatMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String ruaReply = response.body().getResponse();
                    updateChat(ruaReply, false);
                } else {
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
        messageList.add(new Message(message, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }
}
