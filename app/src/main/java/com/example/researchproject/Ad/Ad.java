package com.example.researchproject.Ad;

public class Ad {
    private String adId;
    private String title;
    private String imageUrl;
    private Long timestamp;

    // 🔹 Constructor không tham số bắt buộc để Firebase có thể khởi tạo đối tượng
    public Ad() {
        // Firebase yêu cầu một constructor rỗng
    }

    // 🔹 Constructor có 3 tham số
    public Ad(String adId, String title, String imageUrl) {
        this.adId = adId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis(); // Gán thời gian hiện tại nếu không có timestamp
    }

    // 🔹 Constructor có đầy đủ thông tin
    public Ad(String adId, String title, String imageUrl, Long timestamp) {
        this.adId = adId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // 🔹 Getter và Setter (Firebase yêu cầu)
    public String getAdId() { return adId; }
    public void setAdId(String adId) { this.adId = adId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
