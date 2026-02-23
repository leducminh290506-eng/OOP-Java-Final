package com.oop.project.repository;

import com.oop.project.model.User;
import com.oop.project.model.Role;
import com.oop.project.util.Constants; // Sử dụng file Constants bạn đã tạo

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    // Hàm tìm user theo username từ Database
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(
                Constants.DB_URL, Constants.DB_USER, Constants.DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Lấy dữ liệu từ các cột trong SQL của bạn
                    User user = new User(
                        rs.getInt("user_id"), 
                        rs.getString("username"),
                        rs.getString("password_hash"), // Cột chứa mật khẩu băm
                        Role.valueOf(rs.getString("role").toUpperCase())
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối Database khi tìm User: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Hàm lấy tất cả user (hữu ích cho chức năng Admin quản lý nhóm)
    public List<User> findAll() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(
                Constants.DB_URL, Constants.DB_USER, Constants.DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    Role.valueOf(rs.getString("role").toUpperCase())
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }
}