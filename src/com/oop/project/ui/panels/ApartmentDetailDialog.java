package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * ApartmentDetailDialog - Tóm tắt chi tiết căn hộ.
 */
public class ApartmentDetailDialog extends JDialog {

    public ApartmentDetailDialog(Window owner, Apartment apartment, List<String> amenities) {
        super(owner, "Apartment Detail - " + apartment.getListingCode(), ModalityType.APPLICATION_MODAL);
        setSize(480, 580);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER (Banner nổi bật) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(41, 128, 185)); // Màu xanh dương chủ đạo
        headerPanel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel lblTitle = new JLabel("APARTMENT " + apartment.getListingCode());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hiển thị Category (FR-4.3) và Type
        JLabel lblCategory = new JLabel(apartment.getCategory().toUpperCase() + " | " + apartment.getType());
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCategory.setForeground(new Color(236, 240, 241));
        lblCategory.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(lblCategory);

        // --- 2. BODY (Nội dung chi tiết) ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 2.1 Thông tin cơ bản
        bodyPanel.add(createSectionTitle("Basic Information"));
        bodyPanel.add(createInfoRow("Address:", apartment.getAddress()));
        bodyPanel.add(createInfoRow("Location:", apartment.getLocation()));
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách

        // 2.2 Chi tiết giá (FR-4.2)
        double price = apartment.getPrice();
        int area = apartment.getArea();
        double pricePerSqft = (area > 0) ? (price / area) : 0.0;

        bodyPanel.add(createSectionTitle("Price & Space Details"));
        bodyPanel.add(createInfoRow("Rent Price (Monthly):", String.format("$%,.2f", price)));
        bodyPanel.add(createInfoRow("Bedrooms:", String.valueOf(apartment.getBedrooms())));
        bodyPanel.add(createInfoRow("Area:", area + " sqft"));
        bodyPanel.add(createInfoRow("Price / sqft:", String.format("$%,.2f", pricePerSqft)));
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2.3 Tiện ích đi kèm (Amenities)
        bodyPanel.add(createSectionTitle("Amenities"));
        JPanel amenitiesPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // Lưới 2 cột
        amenitiesPanel.setBackground(Color.WHITE);
        amenitiesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        if (amenities == null || amenities.isEmpty()) {
            amenitiesPanel.add(new JLabel("No amenities available"));
        } else {
            for (String a : amenities) {
                JLabel lblAmenity = new JLabel("✔️ " + a);
                lblAmenity.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblAmenity.setForeground(new Color(52, 73, 94));
                amenitiesPanel.add(lblAmenity);
            }
        }
        bodyPanel.add(amenitiesPanel);

        // Bọc Body trong ScrollPane đề phòng danh sách tiện ích quá dài
        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null); // Xóa viền thừa
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- 3. FOOTER (Nút hành động) ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(new Color(231, 76, 60)); // Màu đỏ
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setOpaque(true);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(100, 35));
        
        // Hiệu ứng hover cho nút Close
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnClose.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnClose.setBackground(new Color(231, 76, 60));
            }
        });
        btnClose.addActionListener(e -> dispose());

        footerPanel.add(btnClose);

        // Lắp ráp các thành phần
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // --- CÁC HÀM TIỆN ÍCH DÀN TRANG UI ---

    private JLabel createSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(41, 128, 185));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return lbl;
    }

    private JPanel createInfoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));

        // Thêm đường gạch chân mờ mờ cho từng dòng (Optional)
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(2, 0, 8, 0)
        ));

        JLabel lblKey = new JLabel(key);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKey.setForeground(new Color(127, 140, 141)); // Màu xám nhạt cho Key

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblValue.setForeground(new Color(44, 62, 80)); // Màu tối cho Value

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        return row;
    }
}