package com.oop.project.repository;

import com.oop.project.exception.DatabaseException;
import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class ApartmentRepository implements IRepository<Apartment, Integer> {

    // ========================================================
    // 1. HÀM MAPPING (Chuyển dòng SQL thành Object 9 tham số)
    // ========================================================
    private Apartment mapResultSetToApartment(ResultSet rs) throws SQLException {
        return new Apartment(
            rs.getInt("apartment_id"),      // Khớp schema.sql
            rs.getString("listing_code"),
            rs.getString("address"),
            rs.getString("location"),
            rs.getDouble("price"),
            rs.getInt("bedrooms"),
            rs.getInt("size_sqft"),         // Khớp schema.sql
            ApartmentType.valueOf(rs.getString("category").toUpperCase()),
            rs.getInt("created_by")
        );
    }

    // ========================================================
    // 2. CRUD CƠ BẢN (THÊM, SỬA, XÓA, TÌM KIẾM)
    // ========================================================
    @Override
    public List<Apartment> findAll() {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments ORDER BY apartment_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToApartment(rs));
        } catch (SQLException e) { throw new DatabaseException("Error fetching all apartments", e); }
        return list;
    }

    public Apartment findById(int id) {
        String sql = "SELECT * FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToApartment(rs);
            }
        } catch (SQLException e) { throw new DatabaseException("Error finding apartment by id", e); }
        return null;
    }

    @Override
    public Optional<Apartment> findById(Integer id) {
        Apartment apt = findById(id.intValue());
        return apt != null ? Optional.of(apt) : Optional.empty();
    }

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
        } catch (SQLException e) { throw new DatabaseException("Error saving apartment", e); }
    }

    @Override
    public void update(Apartment apt) {
        String sql = "UPDATE apartments SET listing_code=?, address=?, location=?, price=?, bedrooms=?, size_sqft=?, category=? WHERE apartment_id=?";
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
        } catch (SQLException e) { throw new DatabaseException("Error updating apartment", e); }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Error deleting apartment", e); }
    }

    // ========================================================
    // 3. LOGIC CHO FAVORITE (YÊU THÍCH) - Sửa lỗi đỏ màn hình trước
    // ========================================================
    public List<Apartment> getFavorites(int userId) {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT a.* FROM apartments a JOIN favorites f ON a.apartment_id = f.apartment_id WHERE f.user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) { throw new DatabaseException("Error fetching favorites", e); }
        return list;
    }

    public void toggleFavorite(int userId, int apartmentId) {
        String checkSql = "SELECT 1 FROM favorites WHERE user_id = ? AND apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement check = conn.prepareStatement(checkSql);
            check.setInt(1, userId);
            check.setInt(2, apartmentId);
            if (check.executeQuery().next()) {
                PreparedStatement del = conn.prepareStatement("DELETE FROM favorites WHERE user_id = ? AND apartment_id = ?");
                del.setInt(1, userId); del.setInt(2, apartmentId); del.executeUpdate();
            } else {
                PreparedStatement ins = conn.prepareStatement("INSERT INTO favorites (user_id, apartment_id) VALUES (?, ?)");
                ins.setInt(1, userId); ins.setInt(2, apartmentId); ins.executeUpdate();
            }
        } catch (SQLException e) { throw new DatabaseException("Error toggling favorite", e); }
    }

    // ========================================================
    // 4. LOGIC CHO BỘ LỌC VÀ TIỆN ÍCH (FILTER PANEL)
    // ========================================================
    public List<Apartment> filterAdvanced(Double maxPrice, Integer minBedrooms, String location, List<String> amenities) {
        List<Apartment> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT a.* FROM apartments a");
        
        if (amenities != null && !amenities.isEmpty()) {
            sql.append(" JOIN apartment_amenities aa ON a.apartment_id = aa.apartment_id")
               .append(" JOIN amenities am ON aa.amenity_id = am.amenity_id");
        }
        
        sql.append(" WHERE 1=1");
        if (maxPrice != null) sql.append(" AND a.price <= ").append(maxPrice);
        if (minBedrooms != null) sql.append(" AND a.bedrooms >= ").append(minBedrooms);
        if (location != null && !location.trim().isEmpty()) sql.append(" AND a.location LIKE '%").append(location.trim()).append("%'");
        
        if (amenities != null && !amenities.isEmpty()) {
            sql.append(" AND am.amenity_name IN (");
            for (int i = 0; i < amenities.size(); i++) {
                sql.append(i == 0 ? "'" : ",'").append(amenities.get(i)).append("'");
            }
            sql.append(")");
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) result.add(mapResultSetToApartment(rs));
        } catch (SQLException e) { throw new DatabaseException("Error in advanced filter", e); }
        return result;
    }

    public List<String> findAllAmenityNames() {
        List<String> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT amenity_name FROM amenities ORDER BY amenity_name")) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) { throw new DatabaseException("Error fetching amenities", e); }
        return list;
    }

    public List<String> findAmenityNamesByApartmentId(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT am.amenity_name FROM apartment_amenities aa JOIN amenities am ON aa.amenity_id = am.amenity_id WHERE aa.apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        } catch (SQLException e) { throw new DatabaseException("Error fetching apartment amenities", e); }
        return list;
    }

    public void seedApartmentAmenitiesIfEmpty() {
        // Hàm này giữ trống để tránh làm hỏng database nếu bạn đã chạy seed.sql
    }
    
    // Hàm tìm kiếm theo keyword (nếu ListingPanel của bạn có ô Search)
    public List<Apartment> searchByKeyword(String keyword) {
        List<Apartment> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return findAll();
        String sql = "SELECT DISTINCT * FROM apartments WHERE listing_code LIKE ? OR address LIKE ? OR location LIKE ?";
        String like = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setString(3, like);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) { throw new DatabaseException("Error searching", e); }
        return result;
    }
}