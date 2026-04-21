package com.oop.project.repository;

import com.oop.project.model.LeaseContract;
import com.oop.project.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContractRepository {

    public void save(LeaseContract contract) throws SQLException {
        // Đổi customer_id thành customer_name
        String sql = "INSERT INTO lease_contracts (apartment_id, customer_name, start_date, end_date, monthly_rent, total_value, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, contract.getApartmentId());
            stmt.setString(2, contract.getCustomerName()); // Truyền tên dạng Chuỗi (String)
            stmt.setDate(3, Date.valueOf(contract.getStartDate()));
            stmt.setDate(4, Date.valueOf(contract.getEndDate()));
            stmt.setDouble(5, contract.getMonthlyRent());
            stmt.setDouble(6, contract.getTotalValue());
            stmt.setString(7, contract.getNotes());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) contract.setContractId(rs.getInt(1));
            }
        }
    }

    public int countActiveContracts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM lease_contracts WHERE end_date >= CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public double getTotalMonthlyRevenue() throws SQLException {
        String sql = "SELECT SUM(monthly_rent) FROM lease_contracts WHERE end_date >= CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public List<LeaseContract> findAll() throws SQLException {
        List<LeaseContract> list = new ArrayList<>();
        String sql = "SELECT * FROM lease_contracts ORDER BY start_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new LeaseContract(
                        rs.getInt("id"),
                        rs.getInt("apartment_id"),
                        rs.getString("customer_name"), // Lấy tên khách hàng từ DB
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getDouble("monthly_rent"),
                        rs.getString("notes")
                ));
            }
        }
        return list;
    }

    public void delete(int contractId) throws SQLException {
        String sql = "DELETE FROM lease_contracts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contractId);
            stmt.executeUpdate();
        }
    }

    public LeaseContract findById(int contractId) throws SQLException {
        String sql = "SELECT * FROM lease_contracts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contractId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new LeaseContract(
                            rs.getInt("id"),
                            rs.getInt("apartment_id"),
                            rs.getString("customer_name"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getDouble("monthly_rent"),
                            rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
}