package com.example.researchproject.Post;
public class Post {
    private String title, serviceInfo, price, rentalTime, address, contact, imageUrl;
    private long timestamp; // ✅ Thêm timestamp
    private String postId, userEmail;
    // Constructor mặc định (cần cho Firebase)
    public Post() {}

    // Constructor đầy đủ
    public Post(String postId, String title, String serviceInfo, String price, String rentalTime, String address, String contact, String imageUrl,long timestamp,String userEmail) {
        this.postId = postId;
        this.title = title;
        this.serviceInfo = serviceInfo;
        this.price = price;
        this.rentalTime = rentalTime;
        this.address = address;
        this.contact = contact;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.userEmail=userEmail;

    }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    // Getter và Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getServiceInfo() { return serviceInfo; }
    public void setServiceInfo(String serviceInfo) { this.serviceInfo = serviceInfo; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getRentalTime() { return rentalTime; }
    public void setRentalTime(String rentalTime) { this.rentalTime = rentalTime; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Getter và Setter cho timestamp
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;}
}