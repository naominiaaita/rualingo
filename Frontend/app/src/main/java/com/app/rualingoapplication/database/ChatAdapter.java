package com.app.rualingoapplication.database;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.rualingoapplication.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_RUA = 2;
    public interface OnChatClickListener {
        void onSpeakClick(String text);
    }

    private final List<Message> messageList;
    private final OnChatClickListener listener;

    public ChatAdapter(List<Message> messageList, OnChatClickListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? TYPE_USER : TYPE_RUA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_rua, parent, false);
            return new RuaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String text = message.getText();
        
        // Strip backend prefix if present
        if (text != null && text.startsWith("Rua says:")) {
            text = text.replaceFirst("Rua says:\\s*", "");
        }

        // Logic for speaker icon & avatar grouping
        boolean isLastOfSpeaker = true;
        if (position < messageList.size() - 1) {
            Message nextMessage = messageList.get(position + 1);
            if (nextMessage.isUser() == message.isUser()) {
                isLastOfSpeaker = false;
            }
        }

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessage.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else if (holder instanceof RuaViewHolder) {
            RuaViewHolder ruaHolder = (RuaViewHolder) holder;
            ruaHolder.tvMessage.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
            
            // Show speaker only if it's the last message in this sequence
            ruaHolder.btnSpeak.setVisibility(isLastOfSpeaker ? View.VISIBLE : View.GONE);
            
            // Hide avatar if not the last message in the sequence
            ruaHolder.ruaAvatar.setVisibility(isLastOfSpeaker ? View.VISIBLE : View.INVISIBLE);

            ruaHolder.btnSpeak.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSpeakClick(message.getText()); 
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    static class RuaViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        android.widget.ImageButton btnSpeak;
        View ruaAvatar;
        RuaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnSpeak = itemView.findViewById(R.id.btnSpeak);
            ruaAvatar = itemView.findViewById(R.id.ruaAvatar);
        }
    }
}

