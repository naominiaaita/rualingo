package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ViewHolder> {

    private final List<Map<String, Object>> logs;

    public ActivityLogAdapter(List<Map<String, Object>> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> log = logs.get(position);
        String action = (String) log.get("action");
        Object lessonId = log.get("lessonId");
        Object timestamp = log.get("timestamp");

        // UI Text Mapping
        if ("LESSON_COMPLETED".equals(action)) {
            holder.actionText.setText("Lesson Completed! 🇵🇳");
            holder.icon.setImageResource(android.R.drawable.ic_menu_today);
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.duo_green));
            holder.detailsText.setText("Finished a learning unit.");
        } else if ("USER_LOGIN".equals(action)) {
            holder.actionText.setText("Daily Session Started");
            holder.icon.setImageResource(android.R.drawable.ic_lock_power_off);
            holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.duo_blue));
            holder.detailsText.setText("Logged in to keep the streak!");
        } else {
            // Filter out technical API paths (starting with /, POST, GET, PUT)
            if (action != null && (action.startsWith("/") || action.startsWith("POST") || action.startsWith("GET") || action.startsWith("PUT"))) {
                holder.actionText.setText("App Activity");
                holder.detailsText.setText("General maintenance & sync");
            } else {
                holder.actionText.setText(action != null ? action : "Activity");
                holder.detailsText.setText("Learning progress tracked");
            }
        }

        holder.timeText.setText(timestamp != null ? timestamp.toString() : "Recent");
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView actionText, detailsText, timeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.logIcon);
            actionText = itemView.findViewById(R.id.logAction);
            detailsText = itemView.findViewById(R.id.logDetails);
            timeText = itemView.findViewById(R.id.logTime);
        }
    }
}
