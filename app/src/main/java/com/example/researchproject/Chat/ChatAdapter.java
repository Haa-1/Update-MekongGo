package com.example.researchproject.Chat;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.noties.markwon.Markwon;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> chatMessages;
    private final Markwon markwon; // 👉 Markwon để hiển thị Markdown

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        // ✅ Khởi tạo Markwon

        // ✅ Cấu hình Markwon hỗ trợ ảnh và URL
        this.markwon = Markwon.builder(context)
                .usePlugin(ImagesPlugin.create())
                .usePlugin(GlideImagesPlugin.create(Glide.with(context))) // Glide để tải ảnh
                .usePlugin(LinkifyPlugin.create()) // Tự động nhận diện URL
                .build();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        // 🔥 Nếu là phản hồi từ AI → áp dụng Markdown
        if (!message.isUser()) {
            markwon.setMarkdown(holder.txtMessage, message.getMessage());
        } else {
            // 💬 Nếu là người dùng → chỉ hiển thị văn bản bình thường
            holder.txtMessage.setText(message.getMessage());
        }

        // 🎨 Tạo Spannable để làm nổi bật từ khóa và emoji
        SpannableString spannable = new SpannableString(holder.txtMessage.getText());

        // 🎉 List emoji hoặc từ khóa cần làm nổi bật
        String[] highlights = {"Meko AI", "😊", "🎉", "🚀", "🔥", "💡"};

        for (String highlight : highlights) {
            int index = message.getMessage().indexOf(highlight);
            while (index >= 0) {
                if (highlight.length() > 0) { // ✅ Kiểm tra độ dài
                    spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#1569AB")),
                            index, index + highlight.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                index = message.getMessage().indexOf(highlight, index + 1); // Tìm các vị trí tiếp theo
            }
        }


        // 👉 Gán lại sau khi chỉnh định dạng
        holder.txtMessage.setText(spannable);

        // 💡 Đổi avatar và màu nền dựa theo AI hoặc User
        if (message.isUser()) {
            holder.imgAvatar.setImageResource(R.drawable.user); // Icon người dùng
            holder.txtMessage.setBackgroundResource(R.drawable.bg_chat_bubble_user); // Nền màu xanh
        } else {
            holder.imgAvatar.setImageResource(R.drawable.search_icon); // Icon AI
            holder.txtMessage.setBackgroundResource(R.drawable.bg_chat_bubble_ai); // Nền màu xám
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageView imgAvatar;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}