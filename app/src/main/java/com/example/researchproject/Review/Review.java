package com.example.researchproject.Review;
public class Review {
    private String user;
    private String comment;
    private float rating;
    private String imageUrl;

    // Constructor mặc định cần thiết cho Firebase
    public Review() {
    }

    public Review( String user,String comment, float rating, String imageUrl) {
        this.user=user;
        this.comment = comment;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}