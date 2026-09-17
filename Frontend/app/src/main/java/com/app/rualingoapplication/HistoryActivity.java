package com.app.rualingoapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.historyRecyclerView);
        emptyText = findViewById(R.id.emptyHistoryText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView title = findViewById(R.id.historyTitle);
        String targetName = getIntent().getStringExtra("targetUserName");
        if (targetName != null && !targetName.isEmpty()) {
            title.setText(targetName + "'s Learning History");
        }

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        long tempId = getIntent().getLongExtra("targetUserId", -1L);
        if (tempId == -1L) {
            tempId = sessionManager.getUserId();
        }
        
        final Long userId = tempId;
        if (userId == -1L) return;

        apiService.viewUserActivity(userId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> logs = response.body();
                    if (logs.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        ActivityLogAdapter adapter = new ActivityLogAdapter(logs);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(HistoryActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
