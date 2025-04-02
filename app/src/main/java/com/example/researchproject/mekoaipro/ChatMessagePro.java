package com.example.researchproject.mekoaipro;
public class ChatMessagePro {
    private String message;
    private boolean isUser; // true nếu là tin nhắn của user, false nếu là chatbot
    private long timestamp; // ✅ Thêm timestamp

    public ChatMessagePro(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;

    }
    public String getMessage() {
        return message;
    }
    public boolean isUser() {
        return isUser;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}