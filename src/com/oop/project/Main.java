package com.oop.project;

import com.oop.project.ui.LoginDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        // flatlaf look and feel
        try {
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());

            // segoe ui font
            java.awt.Font modernFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
            java.util.Enumeration<Object> keys = javax.swing.UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = javax.swing.UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource) {
                    javax.swing.UIManager.put(key, new javax.swing.plaf.FontUIResource(modernFont));
                }
            }

            // rounded corners
            javax.swing.UIManager.put("Button.arc", 10);
            javax.swing.UIManager.put("Component.arc", 10);
            javax.swing.UIManager.put("ProgressBar.arc", 10);
            javax.swing.UIManager.put("TabbedPane.showTabSeparators", true);

        } catch (Exception ex) {
            System.err.println("FlatLaf error: " + ex.getMessage());
        }

        // run
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginDialog().setVisible(true);
        });

        // TEMP: Test MainFrame without login
        // new MainFrame(new User(1, "admin", "role", Role.ADMIN)).setVisible(true);
    }
}