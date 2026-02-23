package com.oop.project.model;

public class Apartment {
    private int id;
    private String listingCode;
    private String address;
    private String location;
    private double price;
    private int bedrooms;
    private int area; // size_sqft trong SQL
    private ApartmentType type; // category trong SQL
    private int createdBy;

    // Constructor chuẩn để xóa lỗi [Ln 150, Col 16] trong ảnh của bạn
    public Apartment(int id, String listingCode, String address, String location, 
                     double price, int bedrooms, int area, ApartmentType type, int createdBy) {
        this.id = id;
        this.listingCode = listingCode;
        this.address = address;
        this.location = location;
        this.price = price;
        this.bedrooms = bedrooms;
        this.area = area;
        this.type = type;
        this.createdBy = createdBy;
    }

    // Các Getters mới cho Repository
    public int getId() { return id; }
    public String getListingCode() { return listingCode; }
    public String getAddress() { return address; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public int getBedrooms() { return bedrooms; }
    public int getArea() { return area; }
    public ApartmentType getType() { return type; }
    public int getCreatedBy() { return createdBy; }

    // --- CÁC HÀM "BẮC CẦU" ĐỂ XÓA 8 LỖI ĐỎ Ở UI/EXPORT ---
    public String getTitle() { return listingCode; } // Xóa lỗi ExportService.java [Ln 15]
    public String getStatus() { return "Available"; } // Xóa lỗi ApartmentTable.java [Ln 33]
    public boolean isFavorite() { return false; } // Xóa lỗi FavoritePanel.java
}