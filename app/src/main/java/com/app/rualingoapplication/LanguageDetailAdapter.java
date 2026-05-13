package com.app.rualingoapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.lblName.setText(lang.getName());
        holder.lblProvince.setText(lang.getProvince() != null ? lang.getProvince() : "N/A");
        holder.lblDistrict.setText(lang.getDistrict() != null ? lang.getDistrict() : "N/A");
        holder.lblClan.setText(lang.getClan() != null ? lang.getClan() : "N/A");
        
        holder.lblLessons.setText(String.valueOf(lang.getLessonCount()));
        holder.lblExercises.setText(String.valueOf(lang.getExerciseCount()));
        holder.lblVocab.setText(String.valueOf(lang.getVocabCount()));
        
        // Quality Metrics
        int audioPct = lang.getExerciseCount() > 0 ? (lang.getAudioCoverage() * 100 / lang.getExerciseCount()) : 0;
        holder.lblAudio.setText(String.format(Locale.getDefault(), "%d%%", audioPct));

        // Dynamic Flag Loading
        int flagResId = 0;
        String flagName = lang.getFlag();
        
        // Manual override for specific local assets if the backend field is generic
        if (lang.getName() != null) {
            if (lang.getName().equalsIgnoreCase("Motu")) {
                flagResId = R.drawable.central_flag;
            } else if (lang.getName().equalsIgnoreCase("Tok Pisin")) {
                flagResId = R.drawable.png_flag;
            }
        }

        if (flagResId == 0 && flagName != null && !flagName.isEmpty()) {
            // Remove extension if present (e.g. "flag.png" -> "flag")
            if (flagName.contains(".")) {
                flagName = flagName.substring(0, flagName.lastIndexOf('.'));
            }
            flagResId = holder.itemView.getContext().getResources().getIdentifier(
                flagName, "drawable", holder.itemView.getContext().getPackageName());
        }
        
        if (flagResId != 0) {
            holder.imgFlag.setImageResource(flagResId);
        } else {
            // Default to Rualingo logo if no specific flag found
            holder.imgFlag.setImageResource(R.drawable.rualingo_logo);
        }

        holder.btnManage.setOnClickListener(v -> listener.onManageClick(lang));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblName, lblProvince, lblDistrict, lblClan, lblLessons, lblExercises, lblVocab, lblAudio;
        ImageView imgFlag;
        View btnManage;

        public ViewHolder(@NonNull View itemView) {
            super(holderView(itemView));
            lblName = itemView.findViewById(R.id.lblLanguageName);
            lblProvince = itemView.findViewById(R.id.lblProvinceName);
            lblDistrict = itemView.findViewById(R.id.lblDistrictName);
            lblClan = itemView.findViewById(R.id.lblClanName);
            lblLessons = itemView.findViewById(R.id.lblLessonCount);
            lblExercises = itemView.findViewById(R.id.lblExerciseCount);
            lblVocab = itemView.findViewById(R.id.lblVocabCount);
            lblAudio = itemView.findViewById(R.id.lblAudioCoverage);
            imgFlag = itemView.findViewById(R.id.imgLanguageFlag);
            btnManage = itemView.findViewById(R.id.btnManageLanguage);
        }

        private static View holderView(View v) { return v; }
    }
}
