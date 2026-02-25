package com.oop.project.service;

import com.oop.project.model.User;
import com.oop.project.repository.UserRepository;
import com.oop.project.util.PasswordUtil;
import com.oop.project.exception.AuthException;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) throws AuthException {
        Optional<User> userOptional = userRepository.findByUsername(username);

        // Sử dụng !isPresent() thay cho isEmpty() để chạy được trên Java 8
        if (!userOptional.isPresent()) {
            throw new AuthException("Tên đăng nhập không tồn tại!");
        }

        User user = userOptional.get();

        // Kiểm tra mật khẩu (Giả sử PasswordUtil có hàm checkPassword)
        // Nếu dùng BCrypt, logic thường là: PasswordUtil.check(password, user.getPasswordHash())
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthException("Mật khẩu không chính xác!");
        }

        return user;
    }
}