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

public class MainFrame extends JFrame {
    private User currentUser;
    private ApartmentService apartmentService;
    private final AuthService authService;
    private boolean isLoggingOut = false;

// Components for Sidebar Navigation
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public MainFrame(User user) {
        this.currentUser = user;
        this.authService = new AuthService(new UserRepository());
        
        ApartmentRepository repo = new ApartmentRepository();
        this.apartmentService = new ApartmentService(repo);

        setTitle("Real Estate System - " + user.getUsername() + " (" + user.getRole() + ")");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!isLoggingOut) {
                    authService.logout(currentUser);
                }
            }
        });
        
        initComponents();
    }

    private void initComponents() {
        // Đặt layout chính của Frame
        setLayout(new BorderLayout());

        // 1. TẠO VÙNG CHỨA NỘI DUNG CHÍNH (CardPanel)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        ListingPanel listingPanel   = new ListingPanel(apartmentService, currentUser);
        FilterPanel filterPanel     = new FilterPanel(apartmentService);
        FavoritePanel favoritePanel = new FavoritePanel(apartmentService, currentUser);
        DashboardPanel dashboard    = new DashboardPanel();

        // Thêm các panel vào cardPanel kèm "Tên định danh" (String)
        cardPanel.add(listingPanel, "Listings");
        cardPanel.add(filterPanel, "Filters");
        cardPanel.add(favoritePanel, "Favorites");
        cardPanel.add(dashboard, "Dashboard");

        if (currentUser.getRole() == Role.ADMIN) { // FR-0.4
            cardPanel.add(new SystemLogPanel(), "SystemLogs");
        }

        // 2. TẠO SIDEBAR BÊN TRÁI
        JPanel sidebar = createSidebar();

        // 3. RÁP VÀO FRAME CHÍNH
        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    // --- HÀM TẠO THANH ĐIỀU HƯỚNG BÊN TRÁI ---
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        // Màu nền tối (Dark Gray/Blue) tạo cảm giác hiện đại
        sidebar.setBackground(new Color(44, 62, 80)); 

        // Logo / Tiêu đề
        JLabel lblLogo = new JLabel("REAL ESTATE");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(30, 0, 40, 0));
        sidebar.add(lblLogo);

// Menu buttons
        sidebar.add(createNavButton("Listings", "Listings"));
        sidebar.add(createNavButton("Filters", "Filters"));
        sidebar.add(createNavButton("Favorites", "Favorites"));
        sidebar.add(createNavButton("Dashboard", "Dashboard"));

        if (currentUser.getRole() == Role.ADMIN) {
            sidebar.add(Box.createVerticalGlue()); // Push Log button to bottom if desired
            sidebar.add(createNavButton("System Logs", "SystemLogs"));
        }

        // Đệm phía dưới
        sidebar.add(Box.createVerticalGlue()); 
        return sidebar;
    }

    // --- HÀM TẠO NÚT CHO SIDEBAR ---
    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        // Loại bỏ giao diện nút mặc định để trông "phẳng" hơn
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(44, 62, 80));
        btn.setForeground(new Color(236, 240, 241));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Căn lề trái cho chữ
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        // Thêm hiệu ứng hover (đổi màu khi trỏ chuột)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 94));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(44, 62, 80));
            }
        });

        // Xử lý sự kiện click: Chuyển màn hình tương ứng bằng CardLayout
        btn.addActionListener(e -> cardLayout.show(cardPanel, cardName));

        return btn;
    }
}