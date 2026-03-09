package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StatisticsService - Cung cấp các số liệu thống kê cho Dashboard (FR-5.4).
 * Hỗ trợ tính toán trung bình giá, đếm theo khu vực, loại hình và lượt yêu thích.
 */
public class StatisticsService {
    private final ApartmentRepository repository;

    public StatisticsService() {
        this.repository = new ApartmentRepository();
    }

    /** Lấy tổng số lượng căn hộ có trong hệ thống */
    public int getTotalApartments() {
        return repository.findAll().size();
    }

    /** * Thống kê số lượng căn hộ theo từng loại hình (FR-5.2)
     * (Luxury, Standard, Budget hoặc Apartment, Studio...)
     */
    public Map<ApartmentType, Long> getCountByType() {
        List<Apartment> all = repository.findAll();
        return all.stream().collect(Collectors.groupingBy(Apartment::getType, Collectors.counting()));
    }

    /** Tính giá trung bình của tất cả căn hộ (FR-5.4) */
    public double getAveragePrice() {
        List<Apartment> all = repository.findAll();
        return all.stream().mapToDouble(Apartment::getPrice).average().orElse(0.0);
    }

    /** * Thống kê số lượng căn hộ theo khu vực/vị trí (FR-5.4)
     * Sử dụng Java Stream để nhóm dữ liệu từ danh sách có sẵn.
     */
    public Map<String, Long> getCountByLocation() {
        List<Apartment> all = repository.findAll();
        return all.stream()
                  .collect(Collectors.groupingBy(
                          Apartment::getLocation,
                          Collectors.counting()
                  ));
    }

    /** * Tính tổng số lượt yêu thích trên toàn hệ thống (FR-5.4)
     * Truy vấn trực tiếp từ bảng favorites để tối ưu hiệu năng.
     */
    public int getTotalFavorites() {
        String sql = "SELECT COUNT(*) AS total FROM favorites";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            // Log lỗi ra console để debug nhưng không làm crash giao diện Dashboard
            System.err.println("Lỗi khi thống kê Favorites: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}