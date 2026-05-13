package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class UserModerationAdapter extends RecyclerView.Adapter<UserModerationAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDeleteClick(User user);
        void onPromoteClick(User user);
    }

    public UserModerationAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_moderation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        String username = user.getUsername() != null ? user.getUsername() : "Anonymous";
        String course = user.getCurrentCourse();
        int streak = user.getStreak();
        String role = user.getRole();
        
        // Role distinction logic: 1 = Admin, 2 = User
        boolean isAdmin = "1".equals(role) || (role != null && role.toUpperCase().contains("ADMIN"));
        
        holder.tvName.setText(username);
        holder.tvStreak.setText(String.valueOf(streak));
        holder.tvEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
        holder.tvProvince.setText("Province: " + (user.getProvinceOfOrigin() != null ? user.getProvinceOfOrigin() : "N/A"));
        holder.tvRole.setText("Account Role: " + (isAdmin ? "Administrator" : "Learner"));
        
        if (isAdmin) {
            holder.statusDot.setVisibility(View.GONE);
            holder.streakContainer.setVisibility(View.GONE);
            holder.tvCourse.setText("Role: Administrator");
            holder.btnPromote.setVisibility(View.GONE);
        } else {
            holder.statusDot.setVisibility(View.VISIBLE);
            holder.streakContainer.setVisibility(View.VISIBLE);
            holder.tvCourse.setText("Learning: " + (course != null && !course.isEmpty() ? course : "N/A"));
            holder.btnPromote.setVisibility(View.VISIBLE);
            
            // Status Dot Color
            if (user.getIsActive() != null && user.getIsActive()) {
                holder.statusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#58A700"))); 
            } else {
                holder.statusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#AFAFAF")));
            }
        }

        // Expand/Collapse Logic
        holder.btnActions.setOnClickListener(v -> {
            boolean isExpanded = holder.detailLayout.getVisibility() == View.VISIBLE;
            holder.detailLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            holder.btnActions.animate().rotation(isExpanded ? 90 : 270).setDuration(200).start();
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(user));
        holder.btnPromote.setOnClickListener(v -> listener.onPromoteClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCourse, tvStreak, tvEmail, tvProvince, tvRole;
        View statusDot, streakContainer, detailLayout;
        ImageButton btnActions;
        MaterialButton btnDelete, btnPromote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvCourse = itemView.findViewById(R.id.tvUserCourse);
            tvStreak = itemView.findViewById(R.id.tvUserStreak);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvProvince = itemView.findViewById(R.id.tvUserProvince);
            tvRole = itemView.findViewById(R.id.tvAccountRole);
            statusDot = itemView.findViewById(R.id.statusDot);
            streakContainer = itemView.findViewById(R.id.streakContainer);
            detailLayout = itemView.findViewById(R.id.detailLayout);
            btnActions = itemView.findViewById(R.id.btnUserActions);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            btnPromote = itemView.findViewById(R.id.btnPromoteAdmin);
        }
    }
}
