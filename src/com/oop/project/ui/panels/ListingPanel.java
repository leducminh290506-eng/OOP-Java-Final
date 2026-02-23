package com.oop.project.ui.panels;

import com.oop.project.model.User;
import com.oop.project.service.ApartmentService;
import com.oop.project.service.ExportService;
import com.oop.project.ui.components.ApartmentTable;
import com.oop.project.ui.components.FilterPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ListingPanel extends JPanel {
    private ApartmentService service;
    private ExportService exportService;
    private ApartmentTable table;
    
    @SuppressWarnings("unused") // Thêm dòng này để hết lỗi vàng
    private User currentUser;
    private FilterPanel filterPanel;

    public ListingPanel(User currentUser) {
        this.currentUser = currentUser;
        this.service = new ApartmentService();
        this.exportService = new ExportService();
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        table = new ApartmentTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        filterPanel = new FilterPanel(e -> filterData());
        add(filterPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadData());
        
        JButton btnFav = new JButton("Add to Favorites");
        btnFav.addActionListener(e -> markFavorite());
        
        JButton btnExport = new JButton("Export CSV");
        btnExport.addActionListener(e -> exportData());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnFav);
        bottomPanel.add(btnExport);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            table.setData(service.getAllApartments());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void filterData() {
        double min = filterPanel.getMinPrice();
        double max = filterPanel.getMaxPrice();
        table.setData(service.filterByPrice(min, max));
    }

    private void markFavorite() {
        int id = table.getSelectedApartmentId();
        if (id != -1) {
            service.toggleFavorite(id, true);
            JOptionPane.showMessageDialog(this, "Added to favorites!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Select an apartment first.");
        }
    }

    private void exportData() {
        try {
            exportService.exportToCSV(service.getAllApartments(), "apartments_export.csv");
            JOptionPane.showMessageDialog(this, "Exported successfully to apartments_export.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }
}