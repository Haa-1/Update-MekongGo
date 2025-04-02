package com.example.researchproject.mekoaipro;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.researchproject.R;
import java.util.ArrayList;
import java.util.List;
public class ChatAdapterPro extends RecyclerView.Adapter<ChatAdapterPro.ChatViewHolder> {
    private List<ChatMessagePro> chatMessages;

    public ChatAdapterPro(List<ChatMessagePro> chatMessages) {
        this.chatMessages = chatMessages != null ? chatMessages : new ArrayList<>(); // Avoid null list
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessagePro message = chatMessages.get(position);
        if (message != null) {
            holder.messageTextView.setText(message.getMessage());
        } else {
            Log.e("ChatAdapterPro", "Null message at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessagePro message = chatMessages.get(position);
        return (message != null && message.isUser()) ? 1 : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.textMessage);
            if (messageTextView == null) {
                Log.e("ChatViewHolder", "textMessage ID is missing in the layout");
            }
        }
    }
}