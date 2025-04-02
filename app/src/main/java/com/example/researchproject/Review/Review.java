package com.example.researchproject.Review;
public class Review {
    private String user;
    private float rating;
    private String comment;
    public Review() {} // Constructor mặc định
    public Review(String user, float rating, String comment) {
        this.user = user;
        this.rating = rating;
        this.comment = comment;
    }
    public String getUser() { return user; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
}