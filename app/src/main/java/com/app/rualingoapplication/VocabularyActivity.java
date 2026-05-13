package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabularyActivity extends AppCompatActivity {

    private VocabularyAdapter adapter;
    private ApiService apiService;
    private final List<VocabularyItem> vocabularyList = new ArrayList<>();
    private String selectedLanguage;
    private Long selectedCourseId = -1L;
    private String selectedTopic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        SessionManager sessionManager = new SessionManager(this);
        selectedLanguage = sessionManager.getSelectedLanguage();
        TextView titleTv = findViewById(R.id.vocabularyTitle);
        if (titleTv != null) {
            titleTv.setText(getString(R.string.vocab_title_format, selectedLanguage));
        }

        RecyclerView recyclerView = findViewById(R.id.vocabularyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VocabularyAdapter(vocabularyList);
        recyclerView.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();
        selectedTopic = getIntent() != null ? getIntent().getStringExtra("topic") : null;
        setupBottomNav();
        fetchLanguageAndLessons();
    }

    private void fetchLanguageAndLessons() {
        apiService.getCourses().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Course course : response.body()) {
                        String title = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
                        String langName = course.getLanguageName() != null ? course.getLanguageName().toLowerCase() : "";
                        String target = selectedLanguage != null ? selectedLanguage.toLowerCase() : "";

                        // Flexible matching: check language name OR if title contains the target language
                        if (!target.isEmpty() && (langName.contains(target) || title.contains(target))) {
                            selectedCourseId = course.getId();
                            break;
                        }
                    }
                    loadVocabulary();
                } else {
                    loadVocabulary();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                loadVocabulary();
            }
        });
    }

    private void loadVocabulary() {
        apiService.getVocabulary(selectedCourseId, selectedTopic).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<VocabularyItem>> call, @NonNull Response<List<VocabularyItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterAndDisplay(response.body());
                } else {
                    vocabularyList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VocabularyItem>> call, @NonNull Throwable t) {
                vocabularyList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void filterAndDisplay(List<VocabularyItem> allItems) {
        vocabularyList.clear();
        
        // Sort by topic
        Collections.sort(allItems, (v1, v2) -> {
            String t1 = v1.getTopic() != null ? v1.getTopic() : "";
            String t2 = v2.getTopic() != null ? v2.getTopic() : "";
            return t1.compareToIgnoreCase(t2);
        });
        
        vocabularyList.addAll(allItems);

        adapter.notifyDataSetChanged();
        
        if (vocabularyList.isEmpty()) {
            Toast.makeText(this, R.string.no_vocabulary_found, Toast.LENGTH_LONG).show();
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.navLearn).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private static class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {
        private final List<VocabularyItem> words;

        VocabularyAdapter(List<VocabularyItem> words) {
            this.words = words;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VocabularyItem item = words.get(position);
            holder.prompt.setText(item.getWord());
            holder.phonetic.setText(item.getPhonetic());
            holder.answer.setText(item.getTranslation());
            holder.example.setText(item.getExampleSentence());
            holder.target.setText(item.getWordTarget());
            
            String topic = item.getTopic() != null ? item.getTopic() : "General";
            holder.topic.setText(topic.toUpperCase());

            // Header logic
            if (position == 0 || !topic.equals(words.get(position - 1).getTopic())) {
                holder.topicHeader.setVisibility(View.VISIBLE);
                holder.topicHeader.setText("TOPIC: " + topic.toUpperCase());
            } else {
                holder.topicHeader.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return words.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView prompt, answer, phonetic, example, target, topic, topicHeader;
            ViewHolder(View itemView) {
                super(itemView);
                prompt = itemView.findViewById(R.id.vocabPrompt);
                phonetic = itemView.findViewById(R.id.vocabPhonetic);
                answer = itemView.findViewById(R.id.vocabAnswer);
                example = itemView.findViewById(R.id.vocabExample);
                target = itemView.findViewById(R.id.vocabTarget);
                topic = itemView.findViewById(R.id.vocabTopic);
                topicHeader = itemView.findViewById(R.id.vocabTopicHeader);
            }
        }
    }
}
