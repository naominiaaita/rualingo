package com.app.rualingoapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageVocabularyActivity extends AppCompatActivity {

    private TextInputEditText wordET, targetET, phoneticET, translationET, exampleET, topicET;
    private AutoCompleteTextView langSpinner, courseSpinner;
    private ApiService apiService;
    private List<LanguageModel> languages = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private List<VocabularyItem> vocabularyList = new ArrayList<>();
    private VocabularyManageAdapter adapter;
    private Long selectedVocabId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vocabulary);

        apiService = RetrofitClient.getApiService();
        bindViews();
        setupRecyclerView();
        loadInitialData();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.createVocabButton).setOnClickListener(v -> createVocab());
        findViewById(R.id.updateVocabButton).setOnClickListener(v -> updateVocab());
    }

    private void bindViews() {
        wordET = findViewById(R.id.vocabWordEditText);
        targetET = findViewById(R.id.vocabTargetEditText);
        phoneticET = findViewById(R.id.vocabPhoneticEditText);
        translationET = findViewById(R.id.vocabTranslationEditText);
        exampleET = findViewById(R.id.vocabExampleEditText);
        topicET = findViewById(R.id.vocabTopicEditText);
        langSpinner = findViewById(R.id.vocabLanguageSpinner);
        courseSpinner = findViewById(R.id.vocabCourseSpinner);
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.vocabRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VocabularyManageAdapter(vocabularyList, new VocabularyManageAdapter.OnVocabActionListener() {
            @Override public void onEdit(VocabularyItem item) { populateFields(item); }
            @Override public void onDelete(VocabularyItem item) { deleteVocab(item); }
        });
        rv.setAdapter(adapter);
    }

    private void loadInitialData() {
        apiService.getLanguages().enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<List<LanguageModel>> call, @NonNull Response<List<LanguageModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    languages = response.body();
                    updateLangSpinner();
                }
            }
            @Override public void onFailure(@NonNull Call<List<LanguageModel>> call, @NonNull Throwable t) {}
        });

        apiService.getCourses().enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courses = response.body();
                    updateCourseSpinner();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {}
        });

        loadVocabulary();
    }

    private void loadVocabulary() {
        apiService.getVocabulary(null, null).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<List<VocabularyItem>> call, @NonNull Response<List<VocabularyItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vocabularyList.clear();
                    List<VocabularyItem> body = response.body();
                    Collections.sort(body, (v1, v2) -> {
                        String t1 = v1.getTopic() != null ? v1.getTopic() : "";
                        String t2 = v2.getTopic() != null ? v2.getTopic() : "";
                        return t1.compareToIgnoreCase(t2);
                    });
                    vocabularyList.addAll(body);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<List<VocabularyItem>> call, @NonNull Throwable t) {}
        });
    }

    private void updateLangSpinner() {
        List<String> names = new ArrayList<>();
        for (LanguageModel l : languages) names.add(l.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        langSpinner.setAdapter(adapter);
    }

    private void updateCourseSpinner() {
        List<String> titles = new ArrayList<>();
        for (Course c : courses) titles.add(c.getTitle());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        courseSpinner.setAdapter(adapter);
    }

    private void populateFields(VocabularyItem item) {
        selectedVocabId = item.getId();
        wordET.setText(item.getWord());
        targetET.setText(item.getWordTarget());
        phoneticET.setText(item.getPhonetic());
        translationET.setText(item.getTranslation());
        exampleET.setText(item.getExampleSentence());
        topicET.setText(item.getTopic());

        // Set spinners
        if (item.getLanguageId() != null) {
            for (LanguageModel l : languages) {
                if (l.getId().equals(item.getLanguageId())) {
                    langSpinner.setText(l.getName(), false);
                    break;
                }
            }
        }
        if (item.getCourseId() != null) {
            for (Course c : courses) {
                if (c.getId().equals(item.getCourseId())) {
                    courseSpinner.setText(c.getTitle(), false);
                    break;
                }
            }
        }
    }

    private void createVocab() {
        VocabularyItem item = getFromFields();
        if (item == null) return;
        apiService.createVocabulary(item).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<VocabularyItem> call, @NonNull Response<VocabularyItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageVocabularyActivity.this, "Created", Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                    clearFields();
                }
            }
            @Override public void onFailure(@NonNull Call<VocabularyItem> call, @NonNull Throwable t) {}
        });
    }

    private void updateVocab() {
        if (selectedVocabId == null) return;
        VocabularyItem item = getFromFields();
        if (item == null) return;
        apiService.updateVocabulary(selectedVocabId, item).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<VocabularyItem> call, @NonNull Response<VocabularyItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageVocabularyActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                    clearFields();
                }
            }
            @Override public void onFailure(@NonNull Call<VocabularyItem> call, @NonNull Throwable t) {}
        });
    }

    private void deleteVocab(VocabularyItem item) {
        apiService.deleteVocabulary(item.getId()).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageVocabularyActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    loadVocabulary();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }

    private VocabularyItem getFromFields() {
        String word = wordET.getText().toString();
        if (word.isEmpty()) return null;

        VocabularyItem item = new VocabularyItem();
        item.setWord(word);
        item.setWordTarget(targetET.getText().toString());
        item.setPhonetic(phoneticET.getText().toString());
        item.setTranslation(translationET.getText().toString());
        item.setExampleSentence(exampleET.getText().toString());
        item.setTopic(topicET.getText().toString());

        String langName = langSpinner.getText().toString();
        for (LanguageModel l : languages) {
            if (l.getName().equals(langName)) {
                item.setLanguageId(l.getId());
                break;
            }
        }

        String courseTitle = courseSpinner.getText().toString();
        for (Course c : courses) {
            if (c.getTitle().equals(courseTitle)) {
                item.setCourseId(c.getId());
                break;
            }
        }

        return item;
    }

    private void clearFields() {
        selectedVocabId = null;
        wordET.setText("");
        targetET.setText("");
        phoneticET.setText("");
        translationET.setText("");
        exampleET.setText("");
        topicET.setText("");
        langSpinner.setText("");
        courseSpinner.setText("");
    }

    private static class VocabularyManageAdapter extends RecyclerView.Adapter<VocabularyManageAdapter.ViewHolder> {
        private final List<VocabularyItem> items;
        private final OnVocabActionListener listener;

        interface OnVocabActionListener { void onEdit(VocabularyItem item); void onDelete(VocabularyItem item); }

        VocabularyManageAdapter(List<VocabularyItem> items, OnVocabActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_vocabulary, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VocabularyItem item = items.get(position);
            holder.word.setText(item.getWord());
            holder.translation.setText(item.getTranslation());
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView word, translation;
            ImageButton btnEdit, btnDelete;
            ViewHolder(View v) {
                super(v);
                word = v.findViewById(R.id.manageVocabWord);
                translation = v.findViewById(R.id.manageVocabTranslation);
                btnEdit = v.findViewById(R.id.btnEditVocab);
                btnDelete = v.findViewById(R.id.btnDeleteVocab);
            }
        }
    }
}
