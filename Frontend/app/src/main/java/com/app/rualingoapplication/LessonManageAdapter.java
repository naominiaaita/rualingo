package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LessonManageAdapter extends RecyclerView.Adapter<LessonManageAdapter.ViewHolder> {

    private final List<Lesson> lessons;
    private final OnLessonActionListener listener;

    public interface OnLessonActionListener {
        void onEdit(Lesson item);
        void onDelete(Lesson item);
    }

    public LessonManageAdapter(List<Lesson> lessons, OnLessonActionListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.title.setText(lesson.getTitle());
        holder.status.setText(lesson.getSubmissionStatus());
        holder.reviewedAt.setText(lesson.getReviewedAt());
        
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(lesson));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(lesson));
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status, reviewedAt;
        View btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.manageLessonTitle);
            status = itemView.findViewById(R.id.manageLessonStatus);
            reviewedAt = itemView.findViewById(R.id.manageLessonReviewedAt);
            btnEdit = itemView.findViewById(R.id.btnEditLesson);
            btnDelete = itemView.findViewById(R.id.btnDeleteLesson);
        }
    }
}