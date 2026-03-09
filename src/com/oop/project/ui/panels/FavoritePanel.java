package com.oop.project.ui.panels;

import com.oop.project.model.User;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.ApartmentTable;

import javax.swing.*;
import java.awt.*;

public class FavoritePanel extends JPanel {
    private ApartmentService service;
    private ApartmentTable table;
    private User currentUser;

    // Sửa lỗi: Nhận service và user từ MainFrame truyền vào
    public FavoritePanel(ApartmentService service, User user) {
        this.service = service;
        this.currentUser = user;
        setLayout(new BorderLayout());
        initComponents();
        loadFavorites(); // Tự động load khi mở panel
    }

    private void initComponents() {
        table = new ApartmentTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnRefresh = new JButton("Refresh Favorites");
        btnRefresh.addActionListener(e -> loadFavorites());
        
        JButton btnRemove = new JButton("Remove from Favorites");
        btnRemove.addActionListener(e -> removeFavorite());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnRemove);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadFavorites() {
        // Sửa lỗi: Truyền ID của user hiện tại vào service
        table.setApartments(service.getFavorites(currentUser.getId()));
    }

    private void removeFavorite() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId != -1) {
            // Sửa lỗi: Truyền đủ 2 tham số userId và apartmentId
            service.toggleFavorite(currentUser.getId(), apartmentId);
            loadFavorites();
            JOptionPane.showMessageDialog(this, "Removed from favorites!");
        } else {
            JOptionPane.showMessageDialog(this, "Please select an apartment.");
        }
    }
}