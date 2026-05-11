package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.repository.ContractRepository;
import java.util.List;

public class ApartmentService {
    private final ApartmentRepository repository;
    private final ContractRepository contractRepo;

    public ApartmentService(ApartmentRepository repository) {
        this.repository = repository;
        this.contractRepo = new ContractRepository();
    }

    /** Lấy toàn bộ danh sách căn hộ */
    public List<Apartment> findAll() {
        return repository.findAll();
    }

    /** Lấy thông tin 1 căn hộ theo ID (Cho nút Detail) */
    public Apartment getById(int id) {
        return repository.findById(id);
    }

    /**
     * Get real-time rental status for an apartment.
     * Checks lease_contracts to determine if apartment is currently rented.
     * Returns "Rented" or "Vacant".
     */
    public String getRentalStatus(int apartmentId) {
        try {
            return contractRepo.isApartmentCurrentlyRented(apartmentId) ? "Rented" : "Vacant";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // --- CÁC HÀM SỬA LỖI CHO FAVORITE PANEL ---

    /** Lấy danh sách căn hộ yêu thích của người dùng (Sửa lỗi trong ảnh) */
    public List<Apartment> getFavorites(int userId) {
        return repository.getFavorites(userId);
    }

    /** Bật/Tắt trạng thái yêu thích */
    public void toggleFavorite(int userId, int apartmentId) {
        repository.toggleFavorite(userId, apartmentId);
    }

    // --- CÁC HÀM SỬA LỖI CHO FILTER PANEL ---

    /** Lọc căn hộ nâng cao */
    public List<Apartment> filterApartments(Double maxPrice, Integer minBedrooms, String location, List<String> amenities) {
        return repository.filterAdvanced(maxPrice, minBedrooms, location, amenities);
    }

    /** Lấy tất cả tên tiện ích (Wifi, Pool...) */
    public List<String> getAllAmenityNames() {
        return repository.findAllAmenityNames();
    }

    /** Lấy tiện ích của 1 căn hộ */
    public List<String> getAmenitiesForApartment(int id) {
        return repository.findAmenityNamesByApartmentId(id);
    }

    /** Khởi tạo dữ liệu tiện ích mẫu */
    public void ensureApartmentAmenitiesIntegrated() {
        repository.seedApartmentAmenitiesIfEmpty();
    }
    public void save(Apartment apt) { repository.save(apt); }
    public void update(Apartment apt) { repository.update(apt); }
    public void delete(int id) { repository.delete(id); }

    /** FR-1.2: Persist amenity selections for an apartment */
    public void saveAmenities(int apartmentId, List<String> amenityNames) {
        repository.saveAmenities(apartmentId, amenityNames);
    }
}