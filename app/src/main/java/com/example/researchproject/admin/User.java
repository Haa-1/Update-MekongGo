package com.example.researchproject.admin;

public class User {
    private String email;
    private String role;
    private String uid;

    public User() { }

    public User(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
    }


    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getUid() { return uid; }
}