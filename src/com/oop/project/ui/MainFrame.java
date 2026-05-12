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
import com.oop.project.ui.panels.ContractPanel; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private User currentUser;
    private ApartmentService apartmentService;
    private final AuthService authService;
    private boolean isLoggingOut = false;
    
    // Biến để lưu nút đang được chọn (Active State)
    private JButton selectedButton;

    // Đưa FavoritePanel lên đây để toàn bộ Class có thể nhìn thấy
    private FavoritePanel favoritePanel;

    // Components for Sidebar Navigation
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public MainFrame(User user) {
        this.currentUser = user;
        this.authService = new AuthService(new UserRepository());
        
        ApartmentRepository repo = new ApartmentRepository();
        this.apartmentService = new ApartmentService(repo);

        setTitle("Real Estate Management System - " + user.getUsername() + " (" + user.getRole() + ")");
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
        setLayout(new BorderLayout());

        // 1. TẠO VÙNG CHỨA NỘI DUNG CHÍNH (CardPanel)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        ListingPanel listingPanel   = new ListingPanel(apartmentService, currentUser);
        FilterPanel filterPanel = new FilterPanel(apartmentService);        
        
        // ĐÃ FIX LỖI Ở ĐÂY: Thêm apartmentService vào cho khớp với bản ContractPanel xịn
        ContractPanel contractPanel = new ContractPanel(currentUser, apartmentService); 
        
        DashboardPanel dashboard    = new DashboardPanel(); 
        
        // Dùng biến toàn cục (không có chữ FavoritePanel ở đầu)
        this.favoritePanel = new FavoritePanel(apartmentService, currentUser);        

        cardPanel.add(listingPanel, "Listings");
        cardPanel.add(filterPanel, "Filters");
        cardPanel.add(this.favoritePanel, "Favorites"); 
        cardPanel.add(contractPanel, "Contracts");
        cardPanel.add(dashboard, "Dashboard");

        if (currentUser.getRole() == Role.ADMIN) { 
            cardPanel.add(new SystemLogPanel(), "SystemLogs");
        }

        // 2. TẠO SIDEBAR BÊN TRÁI
        JPanel sidebar = createSidebar();

        // 3. RÁP VÀO FRAME CHÍNH
        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    // ========================================================
    // 1. HÀM TẠO THANH SIDEBAR (TEXT ONLY)
    // ========================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // ── App title (word-wrapped with HTML) ──
        JLabel titleLabel = new JLabel("<html><div style='text-align:center;'>Real Estate<br>Management</div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 10, 8, 10));
        sidebar.add(titleLabel);

        // ── Greeting: Hello, username ──
        JLabel greetLabel = new JLabel("Hello, " + currentUser.getUsername());
        greetLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        greetLabel.setForeground(new Color(149, 165, 166));
        greetLabel.setHorizontalAlignment(SwingConstants.CENTER);
        greetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        greetLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        greetLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        sidebar.add(greetLabel);

        // ── Thin separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(52, 73, 94));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        // ── Navigation buttons ──
        JButton btnListings = createNavButton("Listings", "Listings");
        sidebar.add(btnListings);
        sidebar.add(createNavButton("Filters", "Filters"));
        sidebar.add(createNavButton("Favorites", "Favorites"));
        sidebar.add(createNavButton("Contracts", "Contracts"));
        sidebar.add(createNavButton("Dashboard", "Dashboard"));

        // System Logs (ADMIN only) — placed BEFORE the glue so it's above Logout
        if (currentUser.getRole() == Role.ADMIN) {
            sidebar.add(createNavButton("System Logs", "SystemLogs"));
        }

        // Đẩy Logout xuống dưới cùng
        sidebar.add(Box.createVerticalGlue());

        // ── LOG OUT button ──
        JButton btnLogout = new JButton("LOG OUT");
        btnLogout.putClientProperty("JButton.buttonType", "none"); // Prevent FlatLaf override
        btnLogout.setMaximumSize(new Dimension(180, 36));
        btnLogout.setPreferredSize(new Dimension(180, 36));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setHorizontalAlignment(SwingConstants.CENTER);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(new Color(231, 76, 60));
            }
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                isLoggingOut = true;
                authService.logout(currentUser);
                dispose();
                new LoginDialog().setVisible(true);
            }
        });

        sidebar.add(btnLogout);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Mặc định cho nút đầu tiên sáng lên khi vừa mở App
        updateButtonStyles(btnListings);

        return sidebar;
    }

    // ========================================================
    // 2. HÀM TẠO NÚT BẤM (TEXT ONLY + CLICK EFFECT)
    // ========================================================
    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        // Style cơ bản
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(44, 62, 80)); // Màu nền Sidebar
        btn.setForeground(new Color(236, 240, 241)); // Màu chữ trắng mờ
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        // Hiệu ứng Hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != selectedButton) {
                    btn.setBackground(new Color(52, 73, 94));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != selectedButton) {
                    btn.setBackground(new Color(44, 62, 80));
                }
            }
        });

        // Sự kiện Click
        btn.addActionListener(e -> {
            // Nếu nút được bấm là "Favorites", thì bắt nó đi lấy dữ liệu mới!
            if (cardName.equals("Favorites")) {
                refreshFavorites();
            }
            
            cardLayout.show(cardPanel, cardName);
            updateButtonStyles(btn); // Làm sáng nút được chọn
        });

        return btn;
    }

    // ========================================================
    // 3. HÀM CẬP NHẬT TRẠNG THÁI NÚT ĐANG CHỌN (ACTIVE STATE)
    // ========================================================
    private void updateButtonStyles(JButton clickedButton) {
        // Trả nút cũ về màu bình thường
        if (selectedButton != null) {
            selectedButton.setBackground(new Color(44, 62, 80));
            selectedButton.setForeground(new Color(236, 240, 241));
            selectedButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 20));
        }

        // Làm sáng nút mới
        selectedButton = clickedButton;
        selectedButton.setBackground(new Color(62, 85, 108)); // Màu xanh sáng
        selectedButton.setForeground(Color.WHITE); // Chữ trắng rõ
        
        // Thêm viền xanh dương bên trái làm điểm nhấn cho nút đang chọn
        selectedButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(52, 152, 219)), // Viền 5px bên trái
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
    }

    public void refreshFavorites() {
        if (favoritePanel != null) {
            favoritePanel.loadFavorites();
        }
    }
}