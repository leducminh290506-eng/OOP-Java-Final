package com.oop.project.repository;

import com.oop.project.exception.DatabaseException;
import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

/**
 * ApartmentRepository - Quản lý truy vấn dữ liệu căn hộ trong Database.
 * Hỗ trợ các chức năng nâng cao như lọc đa điều kiện và yêu thích.
 */
public class ApartmentRepository implements IRepository<Apartment, Integer> {

    @Override
    public void save(Apartment apt) {
        String sql = "INSERT INTO apartments (listing_code, address, location, price, bedrooms, size_sqft, category, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apt.getListingCode());
            stmt.setString(2, apt.getAddress());
            stmt.setString(3, apt.getLocation());
            stmt.setDouble(4, apt.getPrice());
            stmt.setInt(5, apt.getBedrooms());
            stmt.setInt(6, apt.getArea());
            stmt.setString(7, apt.getType().name());
            stmt.setInt(8, apt.getCreatedBy());
            stmt.executeUpdate();
        } catch (SQLException e) { 
            throw new DatabaseException("Error saving apartment", e); 
        }
    }

    @Override
    public List<Apartment> findAll() {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToApartment(rs));
        } catch (SQLException e) { 
            throw new DatabaseException("Error fetching all apartments", e); 
        }
        return list;
    }

    // Xử lý bộ lọc giá (Fix lỗi undefined findByPriceRange)
    public List<Apartment> findByPriceRange(double min, double max) {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE price BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) { 
            throw new DatabaseException("Error filtering by price range", e); 
        }
        return list;
    }

    /**
     * Lọc nâng cao đa điều kiện (FR-2.1 & FR-2.2).
     * Sử dụng StringBuilder để xây dựng câu lệnh SQL linh hoạt.
     */
    public List<Apartment> filterAdvanced(
            Double maxPrice,
            Integer minBedrooms,
            String location,
            List<String> amenities
    ) {
        List<Apartment> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT a.* FROM apartments a");

        boolean filterByAmenities = amenities != null && !amenities.isEmpty();
        if (filterByAmenities) {
            sql.append(" JOIN apartment_amenities aa ON a.apartment_id = aa.apartment_id")
               .append(" JOIN amenities am ON aa.amenity_id = am.amenity_id");
        }

        sql.append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (maxPrice != null) {
            sql.append(" AND a.price <= ?");
            params.add(maxPrice);
        }
        if (minBedrooms != null) {
            sql.append(" AND a.bedrooms >= ?");
            params.add(minBedrooms);
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND a.location LIKE ?");
            params.add("%" + location.trim() + "%");
        }
        if (filterByAmenities) {
            sql.append(" AND am.amenity_name IN (");
            for (int i = 0; i < amenities.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
                params.add(amenities.get(i));
            }
            sql.append(")");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToApartment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error in advanced filtering", e);
        }
        return result;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { 
            throw new DatabaseException("Error deleting apartment", e); 
        }
    }

    @Override
    public Optional<Apartment> findById(Integer id) {
        String sql = "SELECT * FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToApartment(rs));
                return Optional.empty();
            }
        } catch (SQLException e) { 
            throw new DatabaseException("Error findById", e); 
        }
    }

    @Override
    public void update(Apartment apt) {
        String sql = "UPDATE apartments SET listing_code = ?, address = ?, location = ?, price = ?, bedrooms = ?, size_sqft = ?, category = ? WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apt.getListingCode());
            stmt.setString(2, apt.getAddress());
            stmt.setString(3, apt.getLocation());
            stmt.setDouble(4, apt.getPrice());
            stmt.setInt(5, apt.getBedrooms());
            stmt.setInt(6, apt.getArea());
            stmt.setString(7, apt.getType().name());
            stmt.setInt(8, apt.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { 
            throw new DatabaseException("Error updating apartment", e); 
        }
    }

    public List<Apartment> findFavorites(int userId) {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT a.* FROM apartments a JOIN favorites f ON a.apartment_id = f.apartment_id WHERE f.user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) { 
            throw new DatabaseException("Error fetching favorites", e); 
        }
        return list;
    }

    public void toggleFavorite(int userId, int apartmentId) {
        String checkSql = "SELECT favorite_id FROM favorites WHERE user_id = ? AND apartment_id = ?";
        String insertSql = "INSERT INTO favorites (user_id, apartment_id) VALUES (?, ?)";
        String deleteSql = "DELETE FROM favorites WHERE user_id = ? AND apartment_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, apartmentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                            delStmt.setInt(1, userId);
                            delStmt.setInt(2, apartmentId);
                            delStmt.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                            insStmt.setInt(1, userId);
                            insStmt.setInt(2, apartmentId);
                            insStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error toggling favorite status", e);
        }
    }

    private Apartment mapResultSetToApartment(ResultSet rs) throws SQLException {
        return new Apartment(
            rs.getInt("apartment_id"), 
            rs.getString("listing_code"),
            rs.getString("address"), 
            rs.getString("location"),
            rs.getDouble("price"), 
            rs.getInt("bedrooms"),
            rs.getInt("size_sqft"), 
            ApartmentType.valueOf(rs.getString("category").toUpperCase()),
            rs.getInt("created_by")
        );
    }
}