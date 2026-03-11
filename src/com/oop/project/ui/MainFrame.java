package com.oop.project.ui;

import com.oop.project.model.User;
import com.oop.project.model.Role;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.repository.UserRepository;
import com.oop.project.service.AuthService;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.FilterPanel;
import com.oop.project.ui.panels.DashboardPanel;
import com.oop.project.ui.panels.FavoritePanel;
import com.oop.project.ui.panels.ListingPanel;
import com.oop.project.ui.panels.SystemLogPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * MainFrame - Giao diện chính của hệ thống quản lý căn hộ.
 * Đã tích hợp JTabbedPane (FR-6.3) và phân quyền Admin (FR-0.4).
 */
public class MainFrame extends JFrame {
    private User currentUser;
    private ApartmentService apartmentService;
    private JTabbedPane tabbedPane;
    private final AuthService authService;
    private boolean isLoggingOut = false;

    public MainFrame(User user) {
        this.currentUser = user;

        // AuthService để ghi log LOGOUT vào system logs
        this.authService = new AuthService(new UserRepository());
        
        // Khởi tạo tầng dữ liệu tập trung (Sử dụng Repository hiện tại)
        ApartmentRepository repo = new ApartmentRepository();
        this.apartmentService = new ApartmentService(repo);

        // Thiết lập các thuộc tính cơ bản cho JFrame
        setTitle("Real Estate System - " + user.getUsername() + " (" + user.getRole() + ")");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Nếu user đóng cửa sổ trực tiếp thì vẫn ghi log LOGOUT
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!isLoggingOut) {
                    authService.logout(currentUser);
                }
            }
        });
        
        // Khởi tạo các thành phần giao diện
        initComponents();
    }

    private void initComponents() {
        // Khởi tạo JTabbedPane theo yêu cầu FR-6.3
        tabbedPane = new JTabbedPane();

        // 1. KHỞI TẠO CÁC PANEL CHỨC NĂNG
        // Truyền apartmentService dùng chung để đảm bảo đồng bộ dữ liệu
        ListingPanel listingPanel   = new ListingPanel(apartmentService, currentUser);
        FilterPanel filterPanel     = new FilterPanel(apartmentService);
        FavoritePanel favoritePanel = new FavoritePanel(apartmentService, currentUser);
        DashboardPanel dashboard    = new DashboardPanel(); // Cần StatisticsService nếu cần tính toán

        // 2. THÊM CÁC TAB VÀO HỆ THỐNG (FR-6.3)
        // Icon để null nếu bạn chưa có file ảnh cụ thể
        tabbedPane.addTab("Listings",  null, listingPanel, "Quản lý danh sách căn hộ");
        tabbedPane.addTab("Filters",   null, filterPanel, "Lọc căn hộ nâng cao");
        tabbedPane.addTab("Favorites", null, favoritePanel, "Căn hộ đã đánh dấu");
        tabbedPane.addTab("Dashboard", null, dashboard, "Thống kê và phân tích");

        // 3. PHÂN QUYỀN: Chỉ ADMIN mới thấy tab quản lý hệ thống (FR-0.4)
        if (currentUser.getRole() == Role.ADMIN) {
            tabbedPane.addTab("System Logs", null, new SystemLogPanel(), "Nhật ký hệ thống");
        }

        // Đưa TabbedPane vào giữa màn hình
        add(tabbedPane, BorderLayout.CENTER);
        
        // Khởi tạo thanh trạng thái phía dưới
        setupStatusBar();
    }

    private void setupStatusBar() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel lblUser = new JLabel("Đang đăng nhập: " + currentUser.getUsername());
        JLabel lblRole = new JLabel(" | Quyền: " + currentUser.getRole());
        
        // Định dạng màu sắc dựa trên quyền (Tùy chọn thêm để giao diện chuyên nghiệp)
        if (currentUser.getRole() == Role.ADMIN) {
            lblRole.setForeground(Color.RED);
        } else {
            lblRole.setForeground(new Color(0, 102, 0)); // Màu xanh đậm cho Agent
        }

        statusPanel.add(lblUser);
        statusPanel.add(lblRole);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> handleLogout());
        statusPanel.add(btnLogout);
        
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void handleLogout() {
        isLoggingOut = true;
        authService.logout(currentUser); // ghi vào login_logs (System Logs)
        dispose();
        SwingUtilities.invokeLater(() -> new LoginDialog().setVisible(true));
    }
}