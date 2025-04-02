package com.example.researchproject.Notification;

public class Notification {
    private String title;
    private String content;
    private long timestamp;

    public Notification() {}

    public Notification(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}