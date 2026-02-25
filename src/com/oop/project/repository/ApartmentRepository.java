package com.oop.project.repository;

import com.oop.project.exception.DatabaseException;
import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

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
        } catch (SQLException e) { throw new DatabaseException("Error saving", e); }
    }

    @Override
    public List<Apartment> findAll() {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToApartment(rs));
        } catch (SQLException e) { throw new DatabaseException("Error fetching all", e); }
        return list;
    }

    // Xóa lỗi "findByPriceRange is undefined" [image_e45321.png]
    public List<Apartment> findByPriceRange(double min, double max) {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE price BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapResultSetToApartment(rs));
        } catch (SQLException e) { throw new DatabaseException("Error filtering", e); }
        return list;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Error deleting", e); }
    }

    // Các hàm findById, update... viết tương tự
    @Override public Optional<Apartment> findById(Integer id) { return Optional.empty(); }
    @Override public void update(Apartment apt) {}

    public List<Apartment> findFavorites(int userId) {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT a.* FROM apartments a JOIN favorites f ON a.apartment_id = f.apartment_id WHERE f.user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapResultSetToApartment(rs));
        } catch (SQLException e) { throw new DatabaseException("Error favorites", e); }
        return list;
    }

    private Apartment mapResultSetToApartment(ResultSet rs) throws SQLException {
        return new Apartment(
            rs.getInt("apartment_id"), rs.getString("listing_code"),
            rs.getString("address"), rs.getString("location"),
            rs.getDouble("price"), rs.getInt("bedrooms"),
            rs.getInt("size_sqft"), ApartmentType.valueOf(rs.getString("category").toUpperCase()),
            rs.getInt("created_by")
        );
    }
}