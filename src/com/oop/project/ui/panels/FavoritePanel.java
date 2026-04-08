package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;
import com.oop.project.model.User;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.ApartmentDetailDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FavoritePanel extends JPanel {
    private final ApartmentService service;
    private final User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;

    public FavoritePanel(ApartmentService service, User user) {
        this.service = service;
        this.currentUser = user;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        initComponents();
        loadFavorites(); 
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("My Favorite Apartments");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));

        // KHỞI TẠO BẢNG (Đúng 5 cột để tránh lỗi IndexOutOfBounds)
        String[] columns = {"ID", "Code", "Address", "Price", "Area"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        JButton btnDetail = new JButton("View Detail");
        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) new ApartmentDetailDialog(null, service.getById((int) table.getValueAt(row, 0))).setVisible(true);
        });
        
        // Nút Remove để hủy yêu thích ngay tại đây
        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                service.toggleFavorite(currentUser.getId(), (int) table.getValueAt(row, 0));
                loadFavorites(); // Xóa xong load lại luôn
            }
        });

        actionPanel.add(btnRemove);
        actionPanel.add(btnDetail);

        add(lblTitle, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    // HÀM NÀY PHẢI ĐỂ PUBLIC ĐỂ MAINFRAME GỌI ĐƯỢC
    public void loadFavorites() {
        tableModel.setRowCount(0);
        List<Apartment> list = service.getFavorites(currentUser.getId());
        if (list != null) {
            for (Apartment a : list) {
                tableModel.addRow(new Object[]{
                    a.getId(), a.getListingCode(), a.getAddress(), "$" + a.getPrice(), a.getArea() + " m²"
                });
            }
        }
    }
}