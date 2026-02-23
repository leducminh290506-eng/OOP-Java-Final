package com.oop.project.repository;

import com.oop.project.exception.DatabaseException;
import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApartmentRepository implements IRepository<Apartment, Integer> {

    @Override
    public void save(Apartment apt) {
        String sql = "INSERT INTO apartments (title, address, price, area, type, status, created_by, is_favorite) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, apt.getTitle());
            stmt.setString(2, apt.getAddress());
            stmt.setDouble(3, apt.getPrice());
            stmt.setDouble(4, apt.getArea());
            stmt.setString(5, apt.getType().name());
            stmt.setString(6, apt.getStatus());
            stmt.setInt(7, apt.getCreatedBy());
            stmt.setBoolean(8, apt.isFavorite());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error saving apartment", e);
        }
    }

    @Override
    public void update(Apartment apt) {
        String sql = "UPDATE apartments SET title=?, address=?, price=?, area=?, type=?, status=?, is_favorite=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, apt.getTitle());
            stmt.setString(2, apt.getAddress());
            stmt.setDouble(3, apt.getPrice());
            stmt.setDouble(4, apt.getArea());
            stmt.setString(5, apt.getType().name());
            stmt.setString(6, apt.getStatus());
            stmt.setBoolean(7, apt.isFavorite());
            stmt.setInt(8, apt.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating apartment", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM apartments WHERE id=?";
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
        String sql = "SELECT * FROM apartments WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding apartment", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Apartment> findAll() {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all apartments", e);
        }
        return list;
    }

    public List<Apartment> findByPriceRange(double min, double max) {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE price >= ? AND price <= ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering apartments", e);
        }
        return list;
    }

    public List<Apartment> findFavorites() {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE is_favorite = true";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToApartment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching favorites", e);
        }
        return list;
    }

    private Apartment mapResultSetToApartment(ResultSet rs) throws SQLException {
        return new Apartment(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("address"),
            rs.getDouble("price"),
            rs.getDouble("area"),
            ApartmentType.valueOf(rs.getString("type")),
            rs.getString("status"),
            rs.getInt("created_by"),
            rs.getBoolean("is_favorite")
        );
    }
}