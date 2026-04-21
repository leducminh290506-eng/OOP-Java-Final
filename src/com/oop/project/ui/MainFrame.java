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

        // Logo tiêu đề
        JLabel titleLabel = new JLabel("  Real Estate Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.add(titleLabel);

        // create navigation buttons
        JButton btnListings = createNavButton("Listings", "Listings");
        sidebar.add(btnListings);
        sidebar.add(createNavButton("Filters", "Filters"));
        sidebar.add(createNavButton("Favorites", "Favorites"));
        sidebar.add(createNavButton("Contracts", "Contracts"));
        sidebar.add(createNavButton("Dashboard", "Dashboard"));

        // Đẩy các phần sau xuống dưới
        sidebar.add(Box.createVerticalGlue());

        if (currentUser.getRole() == Role.ADMIN) {
            sidebar.add(createNavButton("System Logs", "SystemLogs"));
        }

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