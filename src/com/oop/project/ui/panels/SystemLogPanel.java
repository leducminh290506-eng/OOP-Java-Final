package com.oop.project.ui.panels;

import com.oop.project.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Panel hiển thị các log của hệ thống:
 * - Log đăng nhập / đăng xuất (login_logs)
 * - Log thao tác với căn hộ (audit_logs)
 */
public class SystemLogPanel extends JPanel {

    private JTable loginTable;
    private JTable auditTable;

    public SystemLogPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadLoginLogs();
        loadAuditLogs();
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();

        // Bảng login_logs
        loginTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Username", "Hành động", "Thời gian"}, 0
        ));
        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.add(new JScrollPane(loginTable), BorderLayout.CENTER);
        JButton btnRefreshLogin = new JButton("Refresh login logs");
        btnRefreshLogin.addActionListener(e -> loadLoginLogs());
        loginPanel.add(btnRefreshLogin, BorderLayout.SOUTH);

        // Bảng audit_logs
        auditTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Username", "Mã căn hộ", "Hành động", "Thời gian", "Chi tiết"}, 0
        ));
        JPanel auditPanel = new JPanel(new BorderLayout());
        auditPanel.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        JButton btnRefreshAudit = new JButton("Refresh audit logs");
        btnRefreshAudit.addActionListener(e -> loadAuditLogs());
        auditPanel.add(btnRefreshAudit, BorderLayout.SOUTH);

        tabs.addTab("Nhật ký đăng nhập", loginPanel);
        tabs.addTab("Nhật ký thao tác căn hộ", auditPanel);

        add(tabs, BorderLayout.CENTER);
    }

    private void loadLoginLogs() {
        String sql = "SELECT l.log_id, u.username, l.action, l.timestamp " +
                     "FROM login_logs l JOIN users u ON l.user_id = u.user_id " +
                     "ORDER BY l.timestamp DESC";
        DefaultTableModel model = (DefaultTableModel) loginTable.getModel();
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("log_id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getTimestamp("timestamp")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải login logs: " + e.getMessage());
        }
    }

    private void loadAuditLogs() {
        String sql = "SELECT a.audit_id, u.username, ap.listing_code, a.action, a.timestamp, a.details " +
                     "FROM audit_logs a " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "JOIN apartments ap ON a.apartment_id = ap.apartment_id " +
                     "ORDER BY a.timestamp DESC";
        DefaultTableModel model = (DefaultTableModel) auditTable.getModel();
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("audit_id"),
                        rs.getString("username"),
                        rs.getString("listing_code"),
                        rs.getString("action"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("details")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải audit logs: " + e.getMessage());
        }
    }
}

