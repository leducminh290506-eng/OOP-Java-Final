package com.oop.project.model;

public class Apartment {
    private int id;
    private String title;
    private String address;
    private double price;
    private double area;
    private ApartmentType type;
    private String status; // AVAILABLE, SOLD, RENTED
    private int createdBy;
    private boolean isFavorite;

    public Apartment() {}

    public Apartment(int id, String title, String address, double price, double area, ApartmentType type, String status, int createdBy, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.type = type;
        this.status = status;
        this.createdBy = createdBy;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }
    public ApartmentType getType() { return type; }
    public void setType(ApartmentType type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean isFavorite) { this.isFavorite = isFavorite; }
}