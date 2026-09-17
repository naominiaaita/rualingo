package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VocabularyManageAdapter extends RecyclerView.Adapter<VocabularyManageAdapter.ViewHolder> {

    private final List<VocabularyItem> vocabulary;
    private final OnVocabActionListener listener;

    public interface OnVocabActionListener {
        void onEdit(VocabularyItem item);
        void onDelete(VocabularyItem item);
    }

    public VocabularyManageAdapter(List<VocabularyItem> vocabulary, List<LanguageModel> languages, List<Course> courses, List<Lesson> lessons, OnVocabActionListener listener) {
        this.vocabulary = vocabulary;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_vocabulary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VocabularyItem item = vocabulary.get(position);
        holder.word.setText(item.getWord());
        holder.translation.setText(item.getTranslation());
        holder.info.setText(item.getTopic());
        
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return vocabulary.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView word, translation, info, header;
        View btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.manageVocabWord);
            translation = itemView.findViewById(R.id.manageVocabTranslation);
            info = itemView.findViewById(R.id.manageVocabInfo);
            header = itemView.findViewById(R.id.vocabHeader);
            btnEdit = itemView.findViewById(R.id.btnEditVocab);
            btnDelete = itemView.findViewById(R.id.btnDeleteVocab);
        }
    }
}