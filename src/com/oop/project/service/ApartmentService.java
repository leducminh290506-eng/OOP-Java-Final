package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * ApartmentService - Lớp service cho các thao tác nghiệp vụ với căn hộ.
 * - Lọc nâng cao (FR-2.1, FR-2.2)
 * - Ghi log audit cho CRUD (FR-4.4)
 */
public class ApartmentService {
    private final ApartmentRepository repository;

    public ApartmentService(ApartmentRepository repository) {
        this.repository = repository;
    }

    public void ensureApartmentAmenitiesIntegrated() {
        repository.seedApartmentAmenitiesIfEmpty();
    }

    public List<String> getAllAmenityNames() {
        return repository.findAllAmenityNames();
    }

    public List<String> getAmenitiesForApartment(int apartmentId) {
        return repository.findAmenityNamesByApartmentId(apartmentId);
    }

    public void setAmenitiesForApartment(int apartmentId, List<String> amenityNames) {
        repository.setAmenitiesForApartment(apartmentId, amenityNames);
    }

    public List<Apartment> searchApartments(String keyword) {
        return repository.searchByKeyword(keyword);
    }

    public List<Apartment> getAllApartments() {
        return repository.findAll();
    }

    public List<Apartment> filterByPrice(double min, double max) {
        return repository.findByPriceRange(min, max);
    }

    /**
     * Lọc nâng cao nhiều điều kiện cùng lúc (FR-2.1, FR-2.2).
     */
    public List<Apartment> filterApartments(
            Double maxPrice,
            Integer minBedrooms,
            String location,
            List<String> amenities
    ) {
        return repository.filterAdvanced(maxPrice, minBedrooms, location, amenities);
    }

    public List<Apartment> getFavorites(int userId) {
        return repository.findFavorites(userId);
    }

    public void toggleFavorite(int userId, int apartmentId) {
        repository.toggleFavorite(userId, apartmentId);
    }

    // --- CRUD + Audit logs (FR-4.4) ---

    /**
     * Tạo mới căn hộ và ghi log audit.
     */
    public void createApartment(Apartment apartment, int userId) {
        repository.save(apartment);
        // Ghi log dựa trên listing_code (UNIQUE)
        logAuditByListingCode(userId, apartment.getListingCode(),
                "CREATE", "Created apartment " + apartment.getListingCode());
    }

    public int createApartmentWithAmenities(Apartment apartment, List<String> amenityNames, int userId) {
        int id = repository.saveAndReturnId(apartment);
        repository.setAmenitiesForApartment(id, amenityNames);
        logAuditByApartmentId(userId, id, "CREATE", "Created apartment " + apartment.getListingCode());
        return id;
    }

    /**
     * Cập nhật căn hộ và ghi log audit.
     */
    public void updateApartment(Apartment apartment, int userId) {
        repository.update(apartment);
        logAuditByApartmentId(userId, apartment.getId(),
                "UPDATE", "Updated apartment " + apartment.getListingCode());
    }

    public void updateApartmentWithAmenities(Apartment apartment, List<String> amenityNames, int userId) {
        repository.update(apartment);
        repository.setAmenitiesForApartment(apartment.getId(), amenityNames);
        logAuditByApartmentId(userId, apartment.getId(),
                "UPDATE", "Updated apartment " + apartment.getListingCode());
    }

    /**
     * Xóa căn hộ và ghi log audit.
     */
    public void deleteApartment(int id, int userId) {
        repository.delete(id);
        logAuditByApartmentId(userId, id,
                "DELETE", "Deleted apartment id = " + id);
    }

    public Apartment getApartmentById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy căn hộ với ID = " + id));
    }

    // --- Audit helpers ---

    private void logAuditByApartmentId(int userId, int apartmentId, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, apartment_id, action, details) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, apartmentId);
            stmt.setString(3, action);
            stmt.setString(4, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Không để lỗi log làm vỡ luồng nghiệp vụ chính
            e.printStackTrace();
        }
    }

    private void logAuditByListingCode(int userId, String listingCode, String action, String details) {
        String sql =
                "INSERT INTO audit_logs (user_id, apartment_id, action, details) " +
                "VALUES (?, (SELECT apartment_id FROM apartments WHERE listing_code = ?), ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, listingCode);
            stmt.setString(3, action);
            stmt.setString(4, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
