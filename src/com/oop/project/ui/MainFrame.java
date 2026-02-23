package com.oop.project.ui;

import com.oop.project.model.User;
import com.oop.project.ui.panels.DashboardPanel;
import com.oop.project.ui.panels.FavoritePanel;
import com.oop.project.ui.panels.ListingPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private JTabbedPane tabbedPane;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Apartment Management System - " + user.getRole().name());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        DashboardPanel dashboardPanel = new DashboardPanel();
        ListingPanel listingPanel = new ListingPanel(currentUser);
        FavoritePanel favoritePanel = new FavoritePanel();

        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Listings", listingPanel);
        tabbedPane.addTab("Favorites", favoritePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
}