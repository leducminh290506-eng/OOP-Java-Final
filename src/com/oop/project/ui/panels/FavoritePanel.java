package com.oop.project.ui.panels;

import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.ApartmentTable;

import javax.swing.*;
import java.awt.*;

public class FavoritePanel extends JPanel {
    private ApartmentService service;
    private ApartmentTable table;

    public FavoritePanel() {
        this.service = new ApartmentService();
        setLayout(new BorderLayout());
        initComponents();
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
        table.setData(service.getFavorites());
    }

    private void removeFavorite() {
        int id = table.getSelectedApartmentId();
        if (id != -1) {
            service.toggleFavorite(id, false);
            loadFavorites();
            JOptionPane.showMessageDialog(this, "Removed from favorites!");
        } else {
            JOptionPane.showMessageDialog(this, "Please select an apartment.");
        }
    }
}