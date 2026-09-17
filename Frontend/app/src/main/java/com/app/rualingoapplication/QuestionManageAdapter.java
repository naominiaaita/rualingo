package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuestionManageAdapter extends RecyclerView.Adapter<QuestionManageAdapter.ViewHolder> {

    private final List<Question> questions;
    private final OnQuestionActionListener listener;

    public interface OnQuestionActionListener {
        void onEdit(Question item);
        void onDelete(Question item);
    }

    public QuestionManageAdapter(List<Question> questions, OnQuestionActionListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question q = questions.get(position);
        holder.prompt.setText(q.getQuestion());
        holder.type.setText(q.getType());
        holder.lesson.setText(String.valueOf(q.getLessonId()));
        
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(q));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(q));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView prompt, type, lesson, lessonHeader;
        View btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            prompt = itemView.findViewById(R.id.manageExercisePrompt);
            type = itemView.findViewById(R.id.manageExerciseType);
            lesson = itemView.findViewById(R.id.manageExerciseLesson);
            lessonHeader = itemView.findViewById(R.id.exerciseLessonHeader);
            btnEdit = itemView.findViewById(R.id.btnEditExercise);
            btnDelete = itemView.findViewById(R.id.btnDeleteExercise);
        }
    }
}