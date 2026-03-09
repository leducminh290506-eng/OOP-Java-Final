package com.oop.project.ui.panels;

import com.oop.project.service.StatisticsService;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * DashboardPanel - Màn hình thống kê tổng quan (FR-5.4).
 * Hiển thị các chỉ số về giá, khu vực và mức độ yêu thích của khách hàng.
 */
public class DashboardPanel extends JPanel {
    private StatisticsService statService;
    
    private JLabel lblTotal;
    private JLabel lblAvgPrice;
    private JLabel lblFavorites;
    private JLabel lblByLocation;

    public DashboardPanel() {
        this.statService = new StatisticsService();
        setLayout(new BorderLayout());
        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- PHẦN TRUNG TÂM: CÁC CHỈ SỐ CHÍNH ---
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTotal = new JLabel("Total Apartments: 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        
        lblAvgPrice = new JLabel("Average Price: $0.0");
        lblAvgPrice.setFont(new Font("Arial", Font.BOLD, 18));

        lblFavorites = new JLabel("Total Favorites: 0");
        lblFavorites.setFont(new Font("Arial", Font.BOLD, 18));

        centerPanel.add(lblTotal);
        centerPanel.add(lblAvgPrice);
        centerPanel.add(lblFavorites);

        // --- TIÊU ĐỀ ---
        JLabel lblHeader = new JLabel("DASHBOARD OVERVIEW", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 22));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);
        
        add(centerPanel, BorderLayout.CENTER);

        // --- PHẦN DƯỚI: THỐNG KÊ CHI TIẾT THEO VỊ TRÍ ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        bottomPanel.add(new JLabel("<html><b>Listings per location:</b></html>"), BorderLayout.NORTH);
        
        lblByLocation = new JLabel("By location: (no data)");
        lblByLocation.setVerticalAlignment(SwingConstants.TOP);
        lblByLocation.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Dùng JScrollPane nếu danh sách địa điểm quá dài
        JScrollPane scrollLocation = new JScrollPane(lblByLocation);
        scrollLocation.setPreferredSize(new Dimension(0, 150));
        scrollLocation.setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(scrollLocation, BorderLayout.CENTER);

        // Nút làm mới dữ liệu
        JButton btnRefresh = new JButton("Refresh Statistics");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRefresh.addActionListener(e -> refreshData());
        bottomPanel.add(btnRefresh, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Cập nhật dữ liệu từ StatisticsService lên giao diện (FR-5.4).
     */
    public void refreshData() {
        try {
            // Lấy dữ liệu từ tầng Service
            int total = statService.getTotalApartments();
            double avg = statService.getAveragePrice();
            int totalFav = statService.getTotalFavorites();
            Map<String, Long> byLocation = statService.getCountByLocation();

            // Cập nhật các nhãn văn bản
            lblTotal.setText("Total Apartments: " + total);
            lblAvgPrice.setText("Average Price: $" + String.format("%.2f", avg));
            lblFavorites.setText("Total Favorites: " + totalFav);

            // Xử lý hiển thị danh sách khu vực bằng HTML (FR-5.4)
            if (byLocation == null || byLocation.isEmpty()) {
                lblByLocation.setText("<html><i style='color:gray;'>By location: (no data)</i></html>");
            } else {
                StringBuilder html = new StringBuilder("<html><ul style='margin-left: 10px;'>");
                byLocation.forEach((loc, count) ->
                        html.append("<li><b>")
                            .append(loc)
                            .append("</b>: ")
                            .append(count)
                            .append(" listings</li>"));
                html.append("</ul></html>");
                lblByLocation.setText(html.toString());
            }
        } catch (Exception e) {
            // Tránh vỡ giao diện nếu lỗi database
            System.err.println("Dashboard Refresh Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}