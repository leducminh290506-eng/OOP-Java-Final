package com.oop.project.ui.panels;

import com.oop.project.model.User;
import com.oop.project.model.Role;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.ApartmentTable;
import javax.swing.*;
import java.awt.*;

public class ListingPanel extends JPanel {
    private ApartmentService service;
    private ApartmentTable table;
    private User currentUser;

    public ListingPanel(ApartmentService service, User user) {
        this.service = service;
        this.currentUser = user; // Lưu user để phân quyền
        setLayout(new BorderLayout());
        initComponents();
        loadAllData();
    }

    private void initComponents() {
        table = new ApartmentTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Toolbar phía trên: Lọc và Quản lý ---
        JPanel toolBar = new JPanel(new BorderLayout());
        
        // Cụm lọc giá (Ai cũng dùng được)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Giá từ:"));
        JTextField txtMin = new JTextField(5);
        filterPanel.add(txtMin);
        filterPanel.add(new JLabel("đến:"));
        JTextField txtMax = new JTextField(5);
        filterPanel.add(txtMax);
        JButton btnFilter = new JButton("Lọc");
        filterPanel.add(btnFilter);

        // Cụm quản lý (CHỈ ADMIN MỚI THẤY)
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (currentUser.getRole() == Role.ADMIN) {
            JButton btnAdd = new JButton("Thêm mới");
            JButton btnEdit = new JButton("Sửa");
            JButton btnDelete = new JButton("Xóa");
            
            // Đặt màu sắc cho các nút quản lý để dễ phân biệt
            btnDelete.setForeground(Color.RED);
            
            adminPanel.add(btnAdd);
            adminPanel.add(btnEdit);
            adminPanel.add(btnDelete);

            // Gán sự kiện (Ví dụ nút Xóa)
            btnDelete.addActionListener(e -> handleDelete());
        }

        toolBar.add(filterPanel, BorderLayout.WEST);
        toolBar.add(adminPanel, BorderLayout.EAST);
        add(toolBar, BorderLayout.NORTH);
    }

    private void handleDelete() {
        int selectedId = table.getSelectedApartmentId();
        if (selectedId != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa căn hộ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteApartment(selectedId);
                loadAllData();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ cần xóa!");
        }
    }

    private void loadAllData() {
        try {
            table.setApartments(service.getAllApartments());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}