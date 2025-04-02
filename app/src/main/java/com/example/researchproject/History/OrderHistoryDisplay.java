package com.example.researchproject.History;

public class OrderHistoryDisplay {
    private String title;
    private String imageUrl;
    private String rentalPeriod;
    private int quantity;
    private int totalPrice;
    private String orderId;



    public OrderHistoryDisplay(String orderId)  {
        this.orderId = orderId;
    }

    public OrderHistoryDisplay(String title, String imageUrl, String rentalPeriod, int quantity, int totalPrice, String orderId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.rentalPeriod = rentalPeriod;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderId = orderId;
    }

    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getRentalPeriod() { return rentalPeriod; }
    public int getQuantity() { return quantity; }
    public int getTotalPrice() { return totalPrice; }
    public String getOrderId() { return orderId; }
}