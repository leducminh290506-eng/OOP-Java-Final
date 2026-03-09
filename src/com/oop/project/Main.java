package com.oop.project;

import com.oop.project.ui.LoginDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        // Thiết lập giao diện hệ thống để app trông đẹp hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException 
               | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Chạy ứng dụng trên Event Dispatch Thread (luồng xử lý giao diện của Swing)
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Khởi tạo và hiển thị cửa sổ đăng nhập
            LoginDialog login = new LoginDialog();
            login.setVisible(true);
        });
    }
}