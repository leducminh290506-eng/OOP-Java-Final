package com.oop.project.model;

/**
 * Model Apartment - Đại diện cho thực thể căn hộ trong hệ thống.
 * Đã tích hợp logic phân loại tự động (FR-4.3) và các hàm bổ trợ cho UI/Export.
 */
public class Apartment {
    private int id;
    private String listingCode;
    private String address;
    private String location;
    private double price;
    private int bedrooms;
    private int area; // Tương ứng với size_sqft trong SQL
    private ApartmentType type; // Loại hình (Căn hộ, Studio, v.v.)
    private int createdBy;
    private String category; // Phân loại (Luxury, Standard, Budget) - FR-4.3

    /**
     * Constructor đầy đủ để khởi tạo căn hộ.
     * Tự động thực hiện phân loại dựa trên giá ngay khi khởi tạo (FR-4.3).
     */
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
        
        // Thực hiện phân loại tự động (FR-4.3)
        this.category = classifyByPrice(price);
    }

    /**
     * Logic phân loại căn hộ dựa trên ngưỡng giá (FR-4.3).
     * Ngưỡng: >= 2000 (Luxury), 700-1999 (Standard), < 700 (Budget).
     */
    private String classifyByPrice(double price) {
        if (price >= 2000) {
            return "Luxury";
        } else if (price >= 700) {
            return "Standard";
        } else {
            return "Budget";
        }
    }

    // --- CÁC GETTERS CƠ BẢN ---
    public int getId() { return id; }
    public String getListingCode() { return listingCode; }
    public String getAddress() { return address; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public int getBedrooms() { return bedrooms; }
    public int getArea() { return area; }
    public ApartmentType getType() { return type; }
    public int getCreatedBy() { return createdBy; }
    public String getCategory() { return category; }

    // --- CÁC HÀM BỔ TRỢ ĐỂ XÓA LỖI ĐỎ TẠI CÁC FILE KHÁC ---
    
    /** Xóa lỗi tại ExportService.java [Ln 15] */
    public String getTitle() { 
        return listingCode; 
    } 

    /** Xóa lỗi tại ApartmentTable.java [Ln 33] */
    public String getStatus() { 
        return "Available"; 
    } 

    /** Xóa lỗi tại FavoritePanel.java */
    public boolean isFavorite() { 
        return false; 
    } 

    // --- SETTERS (Nếu cần cập nhật sau khi khởi tạo) ---
    public void setPrice(double price) {
        this.price = price;
        this.category = classifyByPrice(price); // Cập nhật lại phân loại khi giá đổi
    }
}