package com.oop.project.ui.panels;

import com.oop.project.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
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
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250)); // Nền xám nhạt đồng bộ
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding tổng

        initComponents();
        loadLoginLogs();
        loadAuditLogs();
    }

    private void initComponents() {
        // --- 1. HEADER TIEU ĐỀ ---
        JLabel lblHeader = new JLabel("⚙️ Nhật Ký Hệ Thống (System Logs)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(new Color(44, 62, 80));
        lblHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- 2. KHỞI TẠO TABS ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);
        tabs.setFocusable(false);

        // ==========================================
        // TAB 1: NHẬT KÝ ĐĂNG NHẬP (LOGIN LOGS)
        // ==========================================
        loginTable = createStyledTable(new String[]{"ID", "Username", "Hành động", "Thời gian"});
        
        // Căn chỉnh độ rộng cột cho Login Table
        loginTable.getColumnModel().getColumn(0).setMaxWidth(80);
        loginTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        loginTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        loginTable.getColumnModel().getColumn(3).setPreferredWidth(200);

        JPanel loginPanel = new JPanel(new BorderLayout(0, 10));
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane loginScroll = new JScrollPane(loginTable);
        loginScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        loginPanel.add(loginScroll, BorderLayout.CENTER);

        JButton btnRefreshLogin = createStyledButton("🔄 Làm mới Login Logs", new Color(52, 152, 219));
        btnRefreshLogin.addActionListener(e -> loadLoginLogs());
        
        JPanel loginBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginBottom.setOpaque(false);
        loginBottom.add(btnRefreshLogin);
        loginPanel.add(loginBottom, BorderLayout.SOUTH);

        // ==========================================
        // TAB 2: NHẬT KÝ THAO TÁC (AUDIT LOGS)
        // ==========================================
        auditTable = createStyledTable(new String[]{"ID", "Username", "Mã căn hộ", "Hành động", "Thời gian", "Chi tiết"});
        
        // Căn chỉnh độ rộng cột cho Audit Table
        auditTable.getColumnModel().getColumn(0).setMaxWidth(60);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(300); // Chi tiết cần rộng nhất

        JPanel auditPanel = new JPanel(new BorderLayout(0, 10));
        auditPanel.setBackground(Color.WHITE);
        auditPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane auditScroll = new JScrollPane(auditTable);
        auditScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        auditPanel.add(auditScroll, BorderLayout.CENTER);

        JButton btnRefreshAudit = createStyledButton("🔄 Làm mới Audit Logs", new Color(39, 174, 96));
        btnRefreshAudit.addActionListener(e -> loadAuditLogs());
        
        JPanel auditBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        auditBottom.setOpaque(false);
        auditBottom.add(btnRefreshAudit);
        auditPanel.add(auditBottom, BorderLayout.SOUTH);

        // Thêm vào Tabs
        tabs.addTab("🔑 Đăng Nhập / Đăng Xuất", loginPanel);
        tabs.addTab("📝 Thao Tác Dữ Liệu", auditPanel);

        add(tabs, BorderLayout.CENTER);
    }

    // --- HÀM UI: TẠO BẢNG ĐẸP ---
    private JTable createStyledTable(String[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Khóa bảng không cho sửa trực tiếp
            }
        }) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    // Màu dòng xen kẽ (Zebra Stripes)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(new Color(44, 62, 80));
                }
                return c;
            }
        };

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(41, 128, 185));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(236, 240, 241));
        header.setForeground(new Color(44, 62, 80));
        header.setPreferredSize(new Dimension(100, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // Căn giữa cột ID
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        return table;
    }

    // --- HÀM UI: TẠO NÚT BẤM ---
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 40));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    // --- LOGIC TRUY XUẤT DB (Giữ nguyên của bạn) ---

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
            JOptionPane.showMessageDialog(this, "Lỗi tải login logs: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Lỗi tải audit logs: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}