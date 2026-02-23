package com.oop.project.ui;

import com.oop.project.exception.AuthException;
import com.oop.project.model.User;
import com.oop.project.service.AuthService;
import com.oop.project.repository.UserRepository; // Import thêm Repository

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private AuthService authService;

    public LoginDialog() {
        // Khởi tạo UserRepository trước, sau đó truyền vào AuthService
        UserRepository userRepository = new UserRepository();
        this.authService = new AuthService(userRepository);

        setTitle("Login - Apartment Management System");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        btnLogin = new JButton("Login");
        // Sử dụng lambda cho sự kiện click chuột
        btnLogin.addActionListener(e -> handleLogin());
        
        panel.add(new JLabel()); // Ô trống để căn chỉnh nút bấm
        panel.add(btnLogin);

        add(panel);
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try {
            // Gọi logic đăng nhập từ service
            User user = authService.login(username, password);
            
            JOptionPane.showMessageDialog(this, "Welcome " + user.getRole().name());
            this.dispose(); // Đóng cửa sổ Login
            
            // Mở màn hình chính (Đảm bảo bạn đã có class MainFrame)
            // new MainFrame(user).setVisible(true); 
            
        } catch (AuthException ex) {
            // Hiển thị thông báo nếu đăng nhập thất bại (sai user/pass)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Chạy ứng dụng giao diện Swing trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginDialog().setVisible(true);
        });
    }
}