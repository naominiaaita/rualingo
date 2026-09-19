package com.app.rualingoapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VocabularyActivity extends AppCompatActivity {

    private VocabularyAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private final List<VocabularyItem> vocabularyList = new ArrayList<>();
    private String selectedLanguage;
    private Long selectedCourseId = -1L;
    private String selectedTopic = null;
    private android.widget.ProgressBar progressBar;
    private android.widget.TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        sessionManager = new SessionManager(this);
        selectedLanguage = sessionManager.getSelectedLanguage();
        TextView titleTv = findViewById(R.id.vocabularyTitle);
        if (titleTv != null) {
            titleTv.setText(getString(R.string.vocab_title_format, selectedLanguage));
        }

        RecyclerView recyclerView = findViewById(R.id.vocabularyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VocabularyAdapter(vocabularyList);
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.vocabProgressBar);
        tvEmpty = findViewById(R.id.tvEmptyVocab);

        apiService = RetrofitClient.getApiService();
        selectedCourseId = sessionManager.getSelectedCourseId();
        selectedTopic = getIntent() != null ? getIntent().getStringExtra("topic") : null;
        setupBottomNav();
        
        if (selectedCourseId != -1L) {
            loadVocabulary();
        } else {
            fetchLanguageAndLessons();
        }
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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        
        // Step 1: Fetch Completed Lesson IDs
        apiService.getCompletedLessons().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                final List<Long> completedLessonIds = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> lessonMap : response.body()) {
                        Object idObj = lessonMap.get("lessonId");
                        if (idObj == null) idObj = lessonMap.get("lesson_id");

                        if (idObj instanceof Number) {
                            completedLessonIds.add(((Number) idObj).longValue());
                        }
                    }
                }
                
                // Step 2: Fetch all vocabulary for this course and filter
                apiService.getVocabulary(selectedCourseId, selectedTopic, null).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<VocabularyItem>> call, @NonNull Response<List<VocabularyItem>> response) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<VocabularyItem> filtered = new ArrayList<>();
                            
                            // FORCE FIX: Include the current lesson ID from session to show vocab for what they are learning now
                            long currentLessonId = sessionManager.getCurrentLessonId();
                            
                            for (VocabularyItem item : response.body()) {
                                Long itemLessonId = item.getLessonId();
                                // Show item if it has no lesson (general), 
                                // OR if its lesson is completed, 
                                // OR if it belongs to the current active lesson.
                                if (itemLessonId == null || itemLessonId == -1L || 
                                    completedLessonIds.contains(itemLessonId) || 
                                    itemLessonId == currentLessonId) {
                                    filtered.add(item);
                                }
                            }
                            filterAndDisplay(filtered);
                        } else {
                            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                            vocabularyList.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(VocabularyActivity.this, "Error loading vocabulary: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<VocabularyItem>> call, @NonNull Throwable t) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                        vocabularyList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(VocabularyActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                // If we can't get completed lessons, show nothing for privacy/safety
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
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.no_vocabulary_found, Toast.LENGTH_LONG).show();
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.navLearn).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navLanguages).setOnClickListener(v -> {
            startActivity(new Intent(this, LanguageSelectionActivity.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void playAudio(String audioPath) {
        if (audioPath == null || audioPath.isEmpty()) return;
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
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
                    MediaPlayer mp = MediaPlayer.create(this, resId);
                    if (mp != null) mp.start();
                }
            }
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {
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

            if (item.getAudioPath() != null && !item.getAudioPath().isEmpty()) {
                holder.btnPlayAudio.setVisibility(View.VISIBLE);
                holder.btnPlayAudio.setOnClickListener(v -> playAudio(item.getAudioPath()));
            } else {
                holder.btnPlayAudio.setVisibility(View.GONE);
            }

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
            ImageButton btnPlayAudio;
            ViewHolder(View itemView) {
                super(itemView);
                prompt = itemView.findViewById(R.id.vocabPrompt);
                phonetic = itemView.findViewById(R.id.vocabPhonetic);
                answer = itemView.findViewById(R.id.vocabAnswer);
                example = itemView.findViewById(R.id.vocabExample);
                target = itemView.findViewById(R.id.vocabTarget);
                topic = itemView.findViewById(R.id.vocabTopic);
                topicHeader = itemView.findViewById(R.id.vocabTopicHeader);
                btnPlayAudio = itemView.findViewById(R.id.btnPlayAudio);
            }
        }
    }
}
