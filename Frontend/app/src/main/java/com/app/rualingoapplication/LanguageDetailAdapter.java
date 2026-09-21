package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import java.util.Locale;

public class LanguageDetailAdapter extends RecyclerView.Adapter<LanguageDetailAdapter.ViewHolder> {

    private final List<LanguageModel> languages;
    private final OnManageClickListener listener;

    public interface OnManageClickListener {
        void onManageClick(LanguageModel language);
    }

    public LanguageDetailAdapter(List<LanguageModel> languages, OnManageClickListener listener) {
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LanguageModel lang = languages.get(position);
        holder.lblName.setText(lang.getName() != null ? lang.getName() : lang.getLanguageName());
        holder.lblProvince.setText(lang.getProvince() != null ? lang.getProvince() : "N/A");

        String region = lang.getProvince();
        if (lang.getDistrict() != null && !lang.getDistrict().isEmpty()) {
            region = region == null || region.isEmpty() ? lang.getDistrict() : region + " · " + lang.getDistrict();
        }
        holder.lblProvince.setText(region == null || region.isEmpty() ? "Region not specified" : region);
        if (lang.getSource() != null && !lang.getSource().isEmpty()) {
            holder.lblSource.setText(lang.getSource());
            holder.lblSource.setVisibility(View.VISIBLE);
        } else {
            holder.lblSource.setVisibility(View.GONE);
        }
        
        holder.lblLessons.setText(lang.getLessonCount() + " lessons");
        holder.lblExercises.setText(lang.getExerciseCount() + " exercises");
        holder.lblCourses.setText(lang.getCourseCount() + " courses");
        
        // Quality Metrics
        int audioPct = lang.getExerciseCount() > 0 ? (lang.getAudioCoverage() * 100 / lang.getExerciseCount()) : 0;
        holder.lblAudio.setText(String.format(Locale.getDefault(), "%d%% audio", audioPct));

        // Dynamic Flag Loading
        int flagResId = 0;
        String name = lang.getName() != null ? lang.getName() : lang.getLanguageName();
        if (name != null) {
            if (name.equalsIgnoreCase("Motu")) flagResId = R.drawable.central_flag;
            else if (name.equalsIgnoreCase("Tok Pisin")) flagResId = R.drawable.png_flag;
            else if (name.equalsIgnoreCase("Duna")) flagResId = R.drawable.hela_flag;
            else if (name.equalsIgnoreCase("Tiang")) flagResId = R.drawable.newireland_flag;
        }

        if (flagResId == 0 && lang.getFlag() != null && !lang.getFlag().isEmpty()) {
            String flagName = lang.getFlag();
            if (flagName.contains(".")) flagName = flagName.substring(0, flagName.lastIndexOf('.'));
            flagResId = holder.itemView.getContext().getResources().getIdentifier(
                flagName, "drawable", holder.itemView.getContext().getPackageName());
        }
        
        holder.imgFlag.setImageResource(flagResId != 0 ? flagResId : R.drawable.rualingo_logo);

        // RadioButton Selection Logic
        LanguageSelectionActivity activity = (LanguageSelectionActivity) holder.itemView.getContext();
        holder.radioSelect.setChecked(activity.getSelectedLanguage() == lang);
        MaterialCardView card = (MaterialCardView) holder.itemView;
        card.setStrokeColor(activity.getSelectedLanguage() == lang
            ? holder.itemView.getContext().getColor(R.color.png_red)
            : holder.itemView.getContext().getColor(R.color.png_border));
        card.setStrokeWidth(activity.getSelectedLanguage() == lang ? 3 : 1);

        holder.itemView.setOnClickListener(v -> listener.onManageClick(lang));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblName, lblProvince, lblSource, lblLessons, lblExercises, lblCourses, lblAudio;
        ImageView imgFlag;
        RadioButton radioSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblName = itemView.findViewById(R.id.lblLanguageName);
            lblProvince = itemView.findViewById(R.id.lblProvinceName);
            lblSource = itemView.findViewById(R.id.lblLanguageSource);
            lblLessons = itemView.findViewById(R.id.lblLessonCount);
            lblExercises = itemView.findViewById(R.id.lblExerciseCount);
            lblCourses = itemView.findViewById(R.id.lblCourseCount);
            lblAudio = itemView.findViewById(R.id.lblAudioCoverage);
            imgFlag = itemView.findViewById(R.id.imgLanguageFlag);
            radioSelect = itemView.findViewById(R.id.radioSelect);
        }
    }
}
