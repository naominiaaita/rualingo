package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CourseDetailAdapter extends RecyclerView.Adapter<CourseDetailAdapter.ViewHolder> {

    private final List<Course> courses;
    private final OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onEdit(Course course);
    }

    public CourseDetailAdapter(List<Course> courses, OnCourseActionListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.title.setText(course.getTitle());
        holder.language.setText(course.getLanguageName());
        holder.description.setText(course.getDescription());
        holder.moderationNote.setText(course.getModerationNote());
        holder.reviewedAt.setText(course.getReviewedAt());
        
        holder.btnManage.setOnClickListener(v -> listener.onEdit(course));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, language, description, moderationNote, reviewedAt;
        ImageView icon;
        View btnManage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lblCourseTitle);
            language = itemView.findViewById(R.id.lblCourseLanguage);
            description = itemView.findViewById(R.id.lblCourseDescription);
            moderationNote = itemView.findViewById(R.id.lblCourseModerationNote);
            reviewedAt = itemView.findViewById(R.id.lblCourseReviewedAt);
            icon = itemView.findViewById(R.id.imgCourseIcon);
            btnManage = itemView.findViewById(R.id.btnManageCourse);
        }
    }
}