package com.oop.project.ui;

import com.oop.project.model.User;
import com.oop.project.model.Role;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.panels.FavoritePanel;
import com.oop.project.ui.panels.ListingPanel;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private ApartmentService apartmentService;
    private JTabbedPane tabbedPane;

    public MainFrame(User user) {
        this.currentUser = user;
        // Khởi tạo tầng dữ liệu tập trung
        ApartmentRepository repo = new ApartmentRepository();
        this.apartmentService = new ApartmentService(repo);

        setTitle("Real Estate System - " + user.getUsername() + " (" + user.getRole() + ")");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // KHỞI TẠO CÁC PANEL
        // Truyền currentUser xuống để các Panel tự xử lý phân quyền nút bấm
        ListingPanel listingPanel = new ListingPanel(apartmentService, currentUser);
        FavoritePanel favoritePanel = new FavoritePanel(apartmentService, currentUser);

        // SỬ DỤNG CÁC BIẾN (Xóa lỗi "is not used")
        // Thêm vào TabbedPane để hiển thị lên màn hình
        tabbedPane.addTab("Danh sách căn hộ", new ImageIcon(), listingPanel);
        tabbedPane.addTab("Yêu thích của tôi", new ImageIcon(), favoritePanel);

        // PHÂN QUYỀN: Chỉ ADMIN mới thấy tab quản lý hệ thống
        if (currentUser.getRole() == Role.ADMIN) {
            tabbedPane.addTab("Quản lý Log hệ thống", new JPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);
        
        // Thanh trạng thái dưới cùng
        setupStatusBar();
    }

    private void setupStatusBar() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(new JLabel("Đang đăng nhập: " + currentUser.getUsername()));
        statusPanel.add(new JLabel(" | Quyền: " + currentUser.getRole()));
        add(statusPanel, BorderLayout.SOUTH);
    }
}