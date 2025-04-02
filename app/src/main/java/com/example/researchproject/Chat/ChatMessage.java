package com.example.researchproject.Chat;

public class ChatMessage {
    private String message;
    private boolean isUser;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }
    // ✅ Setter cho message
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isUser() {
        return isUser;
    }
}