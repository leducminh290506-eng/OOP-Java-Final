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

    // service and user info are needed to load and manage favorites
    public FavoritePanel(ApartmentService service, User user) {
        this.service = service;
        this.currentUser = user;
        setLayout(new BorderLayout());
        initComponents();
        loadFavorites(); 
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
        // add id to get favorites for current user
        table.setApartments(service.getFavorites(currentUser.getId()));
    }

    private void removeFavorite() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId != -1) {
            // add id and call logic to toggle favorite status
            service.toggleFavorite(currentUser.getId(), apartmentId);
            loadFavorites();
            JOptionPane.showMessageDialog(this, "Removed from favorites!");
        } else {
            JOptionPane.showMessageDialog(this, "Please select an apartment.");
        }
    }
}