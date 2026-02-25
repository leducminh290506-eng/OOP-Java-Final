package com.oop.project.ui.panels;

import com.oop.project.service.StatisticsService;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private StatisticsService statService;
    private JLabel lblTotal;
    private JLabel lblAvgPrice;

    public DashboardPanel() {
        this.statService = new StatisticsService();
        setLayout(new BorderLayout());
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTotal = new JLabel("Total Apartments: 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        
        lblAvgPrice = new JLabel("Average Price: $0.0");
        lblAvgPrice.setFont(new Font("Arial", Font.BOLD, 18));

        centerPanel.add(lblTotal);
        centerPanel.add(lblAvgPrice);

        add(new JLabel("DASHBOARD OVERVIEW", SwingConstants.CENTER), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("Refresh Statistics");
        btnRefresh.addActionListener(e -> refreshData());
        add(btnRefresh, BorderLayout.SOUTH);
    }

    public void refreshData() {
        try {
            int total = statService.getTotalApartments();
            double avg = statService.getAveragePrice();
            lblTotal.setText("Total Apartments: " + total);
            lblAvgPrice.setText("Average Price: $" + String.format("%.2f", avg));
        } catch (Exception e) {
            // Log or ignore if db is empty/unreachable
        }
    }
}