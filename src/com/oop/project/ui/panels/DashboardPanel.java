package com.oop.project.ui.panels;

import com.oop.project.service.StatisticsService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * DashboardPanel - Màn hình thống kê tổng quan.
 */
public class DashboardPanel extends JPanel {
    private StatisticsService statService;

    private JLabel lblTotal;
    private JLabel lblAvgPrice;
    private JLabel lblFavorites;
    private JPanel locationContentPanel; // Panel chứa danh sách location động

    public DashboardPanel() {
        this.statService = new StatisticsService();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250)); // Nền xám nhạt đồng bộ với các tab khác
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- 1. HEADER & NÚT REFRESH ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblHeader = new JLabel("📊 Thống Kê Tổng Quan");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(new Color(44, 62, 80));

        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setOpaque(true);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(130, 40));
        btnRefresh.addActionListener(e -> refreshData());

        headerPanel.add(lblHeader, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. CENTER: CÁC THẺ THỐNG KÊ (CARDS) ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 140));

        lblTotal = new JLabel("0");
        lblAvgPrice = new JLabel("$0.0");
        lblFavorites = new JLabel("0");

        // Tạo 3 thẻ với 3 màu khác nhau
        cardsPanel.add(createStatCard("Tổng Căn Hộ", "🏢", new Color(41, 128, 185), lblTotal));
        cardsPanel.add(createStatCard("Giá Trung Bình", "💵", new Color(39, 174, 96), lblAvgPrice));
        cardsPanel.add(createStatCard("Lượt Yêu Thích", "❤️", new Color(142, 68, 173), lblFavorites));

        // --- 3. BOTTOM: THỐNG KÊ THEO KHU VỰC ---
        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 15));
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel lblLocationTitle = new JLabel("📍 Mật độ danh sách theo khu vực");
        lblLocationTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLocationTitle.setForeground(new Color(44, 62, 80));
        bottomWrapper.add(lblLocationTitle, BorderLayout.NORTH);

        locationContentPanel = new JPanel();
        locationContentPanel.setLayout(new BoxLayout(locationContentPanel, BoxLayout.Y_AXIS));
        locationContentPanel.setBackground(Color.WHITE);

        JScrollPane scrollLocation = new JScrollPane(locationContentPanel);
        scrollLocation.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollLocation.getVerticalScrollBar().setUnitIncrement(16);
        bottomWrapper.add(scrollLocation, BorderLayout.CENTER);

        // Gom Cards và Bottom lại để set layout cho chuẩn
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(cardsPanel, BorderLayout.NORTH);
        mainContent.add(bottomWrapper, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    // --- HÀM TIỆN ÍCH: TẠO THẺ THỐNG KÊ (CARD) ---
    private JPanel createStatCard(String title, String icon, Color bgColor, JLabel lblValue) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Bo góc giả lập (Nếu có thư viện ngoài thì sẽ đẹp hơn, nhưng Swing gốc thì dùng viền mỏng)
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // Tiêu đề & Icon
        JPanel topPart = new JPanel(new BorderLayout());
        topPart.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(255, 255, 255, 210)); // Trắng hơi mờ

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));

        topPart.add(lblTitle, BorderLayout.WEST);
        topPart.add(lblIcon, BorderLayout.EAST);

        // Giá trị
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(Color.WHITE);

        card.add(topPart, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Cập nhật dữ liệu từ StatisticsService lên giao diện.
     */
    public void refreshData() {
        try {
            int total = statService.getTotalApartments();
            double avg = statService.getAveragePrice();
            int totalFav = statService.getTotalFavorites();
            Map<String, Long> byLocation = statService.getCountByLocation();

            lblTotal.setText(String.valueOf(total));
            lblAvgPrice.setText(String.format("$%,.2f", avg));
            lblFavorites.setText(String.valueOf(totalFav));

            // Render lại danh sách khu vực
            locationContentPanel.removeAll();

            if (byLocation == null || byLocation.isEmpty()) {
                JLabel emptyLabel = new JLabel("Chưa có dữ liệu khu vực");
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                emptyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
                emptyLabel.setForeground(Color.GRAY);
                locationContentPanel.add(emptyLabel);
            } else {
                for (Map.Entry<String, Long> entry : byLocation.entrySet()) {
                    JPanel row = new JPanel(new BorderLayout());
                    row.setBackground(Color.WHITE);
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Chiều cao cố định cho mỗi hàng
                    row.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)), // Kẻ vạch dưới
                            new EmptyBorder(10, 20, 10, 20)
                    ));

                    JLabel lblLoc = new JLabel(entry.getKey());
                    lblLoc.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    lblLoc.setForeground(new Color(44, 62, 80));

                    JLabel lblCount = new JLabel(entry.getValue() + " listings");
                    lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    lblCount.setForeground(new Color(127, 140, 141));

                    row.add(lblLoc, BorderLayout.WEST);
                    row.add(lblCount, BorderLayout.EAST);
                    locationContentPanel.add(row);
                }
            }

            // Ép Swing vẽ lại giao diện sau khi thêm bớt Component
            locationContentPanel.revalidate();
            locationContentPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật Dashboard: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}