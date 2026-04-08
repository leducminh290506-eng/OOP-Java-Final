package com.oop.project.model;

public enum ApartmentStatus {
    AVAILABLE("Trống/Sẵn sàng"),
    RENTED("Đã cho thuê"),
    MAINTENANCE("Đang bảo trì");

    private String description;

    ApartmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}