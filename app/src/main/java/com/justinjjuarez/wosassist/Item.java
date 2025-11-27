package com.justinjjuarez.wosassist;

import java.util.List;

public class Item {
    private String id;
    private String title;
    private String description;
    private String location;
    private String date;
    private String price; // NEU
    private List<String> orderPicture;
    private String userID;

    public Item() {
        // Leerer Konstruktor für Firestore
    }

    public Item(String title, String description, String location, String date, String price,
                List<String> orderPicture, String userID) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.price = price; // NEU
        this.orderPicture = orderPicture;
        this.userID = userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() {
        return price; // NEU
    }

    public void setPrice(String price) {
        this.price = price; // NEU
    }

    public List<String> getOrderPicture() {
        return orderPicture;
    }

    public String getUserID() {
        return userID;
    }
}
