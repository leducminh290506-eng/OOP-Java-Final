package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import com.oop.project.service.ApartmentService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FilterPanel - Cung cấp bộ lọc nâng cao cho căn hộ.
 * Hỗ trợ lọc đa điều kiện (FR-2) và cập nhật thời gian thực (FR-6.4).
 */
public class FilterPanel extends JPanel {

    private final ApartmentService apartmentService;
    private final ApartmentTable table;

    private final JTextField txtMaxPrice;
    private final JTextField txtMinBedrooms;
    private final JTextField txtLocation;

    private final Map<String, JCheckBox> amenityCheckboxes = new LinkedHashMap<>();

    public FilterPanel(ApartmentService service) {
        this.apartmentService = service;
        this.table = new ApartmentTable();

        setLayout(new BorderLayout());

        // đảm bảo DB có liên kết apartment_amenities để lọc theo tiện ích không bị rỗng
        apartmentService.ensureApartmentAmenitiesIntegrated();
        
        // --- PHẦN TRÊN: THANH CÔNG CỤ LỌC ---
        JPanel filterBar = new JPanel(new GridBagLayout());
        filterBar.setBorder(BorderFactory.createTitledBorder("Bộ lọc nâng cao"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int col = 0;

        // Giá tối đa (FR-2.1)
        gbc.gridx = col++; gbc.gridy = 0;
        filterBar.add(new JLabel("Giá tối đa ($):"), gbc);
        txtMaxPrice = new JTextField(8);
        gbc.gridx = col++;
        filterBar.add(txtMaxPrice, gbc);

        // Số phòng ngủ tối thiểu (FR-2.1)
        gbc.gridx = col++; 
        filterBar.add(new JLabel("Phòng ngủ tối thiểu:"), gbc);
        txtMinBedrooms = new JTextField(4);
        gbc.gridx = col++;
        filterBar.add(txtMinBedrooms, gbc);

        // Vị trí (FR-2.1)
        gbc.gridx = col++; 
        filterBar.add(new JLabel("Vị trí/Địa chỉ:"), gbc);
        txtLocation = new JTextField(12);
        gbc.gridx = col++;
        filterBar.add(txtLocation, gbc);

        // Tiện ích (FR-2.1)
        gbc.gridx = 0; gbc.gridy = 1;
        filterBar.add(new JLabel("Tiện ích:"), gbc);

        JPanel amenityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        List<String> amenityNames = apartmentService.getAllAmenityNames();
        for (String name : amenityNames) {
            JCheckBox cb = new JCheckBox(name);
            amenityCheckboxes.put(name, cb);
            amenityPanel.add(cb);
        }

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.gridwidth = 5; 
        filterBar.add(amenityPanel, gbc);

        // --- PHẦN DƯỚI: BẢNG KẾT QUẢ ---
        add(filterBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Kích hoạt tính năng cập nhật tức thì (FR-6.4)
        attachRealTimeListeners();
        
        // Tải dữ liệu ban đầu
        reloadData(); 
    }

    /**
     * Gắn bộ lắng nghe sự kiện để thực hiện lọc real-time (FR-6.4).
     */
    private void attachRealTimeListeners() {
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reloadData(); }
            @Override public void removeUpdate(DocumentEvent e) { reloadData(); }
            @Override public void changedUpdate(DocumentEvent e) { reloadData(); }
        };

        // Lắng nghe sự thay đổi trên các ô nhập văn bản
        txtMaxPrice.getDocument().addDocumentListener(docListener);
        txtMinBedrooms.getDocument().addDocumentListener(docListener);
        txtLocation.getDocument().addDocumentListener(docListener);

        // Lắng nghe sự thay đổi trên các CheckBox
        ItemListener itemListener = e -> reloadData();
        for (JCheckBox cb : amenityCheckboxes.values()) {
            cb.addItemListener(itemListener);
        }
    }

    /**
     * Thu thập thông tin từ UI và gọi Service để lọc dữ liệu (FR-2.2).
     */
    private void reloadData() {
        Double maxPrice = null;
        Integer minBedrooms = null;
        String location = txtLocation.getText().trim();

        // Xử lý giá
        try {
            String max = txtMaxPrice.getText().trim();
            if (!max.isEmpty()) maxPrice = Double.parseDouble(max);
        } catch (NumberFormatException ex) {
            // Để trống nếu người dùng gõ ký tự không phải số
        }

        // Xử lý phòng ngủ
        try {
            String minBed = txtMinBedrooms.getText().trim();
            if (!minBed.isEmpty()) minBedrooms = Integer.parseInt(minBed);
        } catch (NumberFormatException ex) {
            // Để trống nếu lỗi định dạng
        }

        // Xử lý tiện ích
        List<String> amenities = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> e : amenityCheckboxes.entrySet()) {
            if (e.getValue().isSelected()) {
                amenities.add(e.getKey());
            }
        }

        try {
            // Gọi Service thực hiện lọc phức hợp (Compound Boolean)
            List<Apartment> filtered = apartmentService.filterApartments(
                    maxPrice,
                    minBedrooms,
                    location.isEmpty() ? null : location,
                    amenities
            );

            // Cập nhật kết quả lên bảng ngay lập tức
            table.setApartments(filtered);
        } catch (RuntimeException ex) {
            // Không để lỗi DB/SQL làm UI "im lặng" trắng bảng
            JOptionPane.showMessageDialog(this, "Lỗi lọc căn hộ: " + ex.getMessage());
        }
    }
}