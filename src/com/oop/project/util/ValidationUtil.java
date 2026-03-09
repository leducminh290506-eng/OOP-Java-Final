package com.oop.project.util;

import com.oop.project.model.Apartment;

public class ValidationUtil {
    
    public static void validateApartment(Apartment apt) {
        if (apt.getTitle() == null || apt.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (apt.getAddress() == null || apt.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if (apt.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (apt.getArea() <= 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
    }
}