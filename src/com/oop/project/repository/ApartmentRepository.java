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

    public List<String> findAllAmenityNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT amenity_name FROM amenities ORDER BY amenity_name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("amenity_name"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching amenities", e);
        }
        return names;
    }

    /**
     * Fallback: nếu DB chưa có liên kết apartment_amenities thì tự gắn tạm
     * để bộ lọc theo tiện ích hoạt động (không ghi đè nếu đã có dữ liệu).
     */
    public void seedApartmentAmenitiesIfEmpty() {
        String countSql = "SELECT COUNT(*) AS c FROM apartment_amenities";
        String aptSql = "SELECT apartment_id FROM apartments ORDER BY apartment_id";
        String amSql = "SELECT amenity_id FROM amenities ORDER BY amenity_id";
        String insertSql = "INSERT IGNORE INTO apartment_amenities (apartment_id, amenity_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int currentCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(countSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) currentCount = rs.getInt("c");
            }
            if (currentCount > 0) return;

            List<Integer> apartmentIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(aptSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) apartmentIds.add(rs.getInt("apartment_id"));
            }

            List<Integer> amenityIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(amSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) amenityIds.add(rs.getInt("amenity_id"));
            }

            if (apartmentIds.isEmpty() || amenityIds.isEmpty()) return;

            Random rnd = new Random();
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                for (Integer aptId : apartmentIds) {
                    int k = Math.min(amenityIds.size(), 2 + rnd.nextInt(Math.min(3, amenityIds.size()))); // 2..4
                    Set<Integer> picked = new HashSet<>();
                    while (picked.size() < k) {
                        picked.add(amenityIds.get(rnd.nextInt(amenityIds.size())));
                    }
                    for (Integer amId : picked) {
                        ins.setInt(1, aptId);
                        ins.setInt(2, amId);
                        ins.addBatch();
                    }
                }
                ins.executeBatch();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error seeding apartment amenities", e);
        }
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
        } catch (SQLException e) { 
            throw new DatabaseException("Error saving apartment", e); 
        }
    }

    public int saveAndReturnId(Apartment apt) {
        String sql = "INSERT INTO apartments (listing_code, address, location, price, bedrooms, size_sqft, category, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, apt.getListingCode());
            stmt.setString(2, apt.getAddress());
            stmt.setString(3, apt.getLocation());
            stmt.setDouble(4, apt.getPrice());
            stmt.setInt(5, apt.getBedrooms());
            stmt.setInt(6, apt.getArea());
            stmt.setString(7, apt.getType().name());
            stmt.setInt(8, apt.getCreatedBy());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new DatabaseException("Failed to retrieve generated apartment_id", null);
        } catch (SQLException e) {
            throw new DatabaseException("Error saving apartment", e);
        }
    }

    public List<String> findAmenityNamesByApartmentId(int apartmentId) {
        List<String> names = new ArrayList<>();
        String sql =
                "SELECT am.amenity_name " +
                "FROM apartment_amenities aa " +
                "JOIN amenities am ON aa.amenity_id = am.amenity_id " +
                "WHERE aa.apartment_id = ? " +
                "ORDER BY am.amenity_name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) names.add(rs.getString("amenity_name"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching apartment amenities", e);
        }
        return names;
    }

    public void setAmenitiesForApartment(int apartmentId, List<String> amenityNames) {
        String deleteSql = "DELETE FROM apartment_amenities WHERE apartment_id = ?";
        String insertSql =
                "INSERT INTO apartment_amenities (apartment_id, amenity_id) " +
                "SELECT ?, amenity_id FROM amenities WHERE amenity_name = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                del.setInt(1, apartmentId);
                del.executeUpdate();
            }

            if (amenityNames == null || amenityNames.isEmpty()) return;

            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                for (String name : amenityNames) {
                    ins.setInt(1, apartmentId);
                    ins.setString(2, name);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error updating apartment amenities", e);
        }
    }

    public List<Apartment> searchByKeyword(String keyword) {
        List<Apartment> result = new ArrayList<>();
        String kw = (keyword == null) ? "" : keyword.trim();
        if (kw.isEmpty()) return findAll();

        String sql =
                "SELECT DISTINCT a.* " +
                "FROM apartments a " +
                "LEFT JOIN apartment_amenities aa ON a.apartment_id = aa.apartment_id " +
                "LEFT JOIN amenities am ON aa.amenity_id = am.amenity_id " +
                "WHERE a.listing_code LIKE ? " +
                "   OR a.address LIKE ? " +
                "   OR a.location LIKE ? " +
                "   OR am.amenity_name LIKE ? " +
                "ORDER BY a.apartment_id DESC";

        String like = "%" + kw + "%";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setString(3, like);
            stmt.setString(4, like);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching apartments", e);
        }
        return result;
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