package com.oop.project.service;

import com.oop.project.exception.AuthException;
import com.oop.project.model.User;
import com.oop.project.repository.UserRepository;
import com.oop.project.util.DatabaseConnection;
import com.oop.project.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * AuthService - Xử lý đăng nhập/đăng xuất.
 * Đã tích hợp ghi log LOGIN/LOGOUT vào bảng login_logs (FR-0.5).
 */
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) throws AuthException {
        // 1. Tìm user theo username
        Optional<User> userOptional = userRepository.findByUsername(username);

        // 2. Kiểm tra user có tồn tại không
        if (!userOptional.isPresent()) {
            throw new AuthException("Tên đăng nhập không tồn tại!");
        }

        User user = userOptional.get();

        // 3. Băm mật khẩu người dùng nhập vào rồi mới so sánh với DB
        String hashedInput = PasswordUtil.hashPassword(password);

        if (!hashedInput.equals(user.getPasswordHash())) {
            throw new AuthException("Mật khẩu không chính xác!");
        }

        // Đăng nhập thành công -> ghi log LOGIN
        logLoginEvent(user.getId(), "LOGIN");
        return user;
    }

    /**
     * Hàm logout để UI gọi khi người dùng thoát ứng dụng.
     */
    public void logout(User user) {
        if (user != null) {
            logLoginEvent(user.getId(), "LOGOUT");
        }
    }

    // --- Helper ghi log vào bảng login_logs ---

    private void logLoginEvent(int userId, String action) {
        String sql = "INSERT INTO login_logs (user_id, action) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Không để lỗi log làm vỡ flow đăng nhập/đăng xuất
            e.printStackTrace();
        }
    }
}
