package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import com.oop.project.service.ApartmentService;
import com.oop.project.service.ExportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FilterPanel - Bộ lọc nâng cao với giao diện hiện đại.
 * Hỗ trợ lọc đa điều kiện và cập nhật thời gian thực.
 */
public class FilterPanel extends JPanel {

    private final ApartmentService apartmentService;
    private final ApartmentTable table;

    private JTextField txtMaxPrice;
    private JTextField txtMinBedrooms;
    private JTextField txtLocation;
    private JComboBox<String> cboCategory;
    private final ExportService exportService = new ExportService();

    private final Map<String, JCheckBox> amenityCheckboxes = new LinkedHashMap<>();
    private List<Apartment> lastFiltered = new ArrayList<>();

    public FilterPanel(ApartmentService service) {
        this.apartmentService = service;
        this.table = new ApartmentTable();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        apartmentService.ensureApartmentAmenitiesIntegrated();

        initComponents();
        attachRealTimeListeners();
        reloadData();
    }

    private void initComponents() {
        // --- KHU VỰC BỘ LỌC (TOP PANEL) ---
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);

        JPanel filterCard = new JPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // 1. Nhóm lọc cơ bản (Row 1)
        JPanel basicFilterPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        basicFilterPanel.setBackground(Color.WHITE);
        basicFilterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        basicFilterPanel.add(createInputGroup("💵 Giá tối đa ($):", txtMaxPrice = createStyledTextField()));
        basicFilterPanel.add(createInputGroup("🛏️ Phòng ngủ (Min):", txtMinBedrooms = createStyledTextField()));
        basicFilterPanel.add(createInputGroup("📍 Vị trí / Địa chỉ:", txtLocation = createStyledTextField()));
        
        cboCategory = new JComboBox<>(new String[]{"All", "Luxury", "Standard", "Budget"});
        cboCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        basicFilterPanel.add(createInputGroup("🏷️ Phân loại:", cboCategory));

        // 2. Nhóm tiện ích (Row 2)
        JPanel amenityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        amenityPanel.setBackground(Color.WHITE);
        amenityPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                "Tiện ích đi kèm",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(44, 62, 80)
        ));
        amenityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<String> amenityNames = apartmentService.getAllAmenityNames();
        for (String name : amenityNames) {
            JCheckBox cb = new JCheckBox(name);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cb.setBackground(Color.WHITE);
            cb.setFocusPainted(false);
            cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            amenityCheckboxes.put(name, cb);
            amenityPanel.add(cb);
        }

        // 3. Thanh công cụ (Nút bấm)
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBar.setBackground(Color.WHITE);
        actionBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClear = createStyledButton("Xóa bộ lọc", new Color(149, 165, 166), Color.WHITE);
        btnClear.addActionListener(e -> clearFilters());
        
        JButton btnExport = createStyledButton("📥 Xuất CSV", new Color(39, 174, 96), Color.WHITE);
        btnExport.addActionListener(e -> exportCsv());

        actionBar.add(btnClear);
        actionBar.add(btnExport);

        // Ghép các thành phần vào Filter Card
        filterCard.add(basicFilterPanel);
        filterCard.add(Box.createRigidArea(new Dimension(0, 15)));
        filterCard.add(amenityPanel);
        filterCard.add(Box.createRigidArea(new Dimension(0, 15)));
        filterCard.add(actionBar);

        topPanel.add(filterCard, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU ---
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // --- CÁC HÀM TIỆN ÍCH UI ---
    
    private JPanel createInputGroup(String labelText, JComponent inputComp) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(44, 62, 80));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(inputComp, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return txt;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 36));
        
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

    private void clearFilters() {
        // Tắt listener tạm thời nếu không muốn reloadData chạy liên tục khi clear
        txtMaxPrice.setText("");
        txtMinBedrooms.setText("");
        txtLocation.setText("");
        cboCategory.setSelectedIndex(0);
        for (JCheckBox cb : amenityCheckboxes.values()) {
            cb.setSelected(false);
        }
        reloadData();
    }

    // --- CÁC HÀM LOGIC GỐC (GIỮ NGUYÊN) ---

    private void attachRealTimeListeners() {
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reloadData(); }
            @Override public void removeUpdate(DocumentEvent e) { reloadData(); }
            @Override public void changedUpdate(DocumentEvent e) { reloadData(); }
        };

        txtMaxPrice.getDocument().addDocumentListener(docListener);
        txtMinBedrooms.getDocument().addDocumentListener(docListener);
        txtLocation.getDocument().addDocumentListener(docListener);

        ItemListener itemListener = e -> reloadData();
        for (JCheckBox cb : amenityCheckboxes.values()) {
            cb.addItemListener(itemListener);
        }
        cboCategory.addItemListener(itemListener);
    }

    private void reloadData() {
        Double maxPrice = null;
        Integer minBedrooms = null;
        String location = txtLocation.getText().trim();

        try {
            String max = txtMaxPrice.getText().trim();
            if (!max.isEmpty()) maxPrice = Double.parseDouble(max);
        } catch (NumberFormatException ignored) {}

        try {
            String minBed = txtMinBedrooms.getText().trim();
            if (!minBed.isEmpty()) minBedrooms = Integer.parseInt(minBed);
        } catch (NumberFormatException ignored) {}

        List<String> amenities = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> e : amenityCheckboxes.entrySet()) {
            if (e.getValue().isSelected()) {
                amenities.add(e.getKey());
            }
        }

        try {
            List<Apartment> filtered = apartmentService.filterApartments(
                    maxPrice, minBedrooms, location.isEmpty() ? null : location, amenities);

            String cat = (String) cboCategory.getSelectedItem();
            if (cat != null && !"All".equalsIgnoreCase(cat)) {
                List<Apartment> byCat = new ArrayList<>();
                for (Apartment a : filtered) {
                    if (cat.equalsIgnoreCase(a.getCategory())) byCat.add(a);
                }
                filtered = byCat;
            }

            lastFiltered = filtered;
            table.setApartments(filtered);
        } catch (RuntimeException ex) {
            System.err.println("Lỗi bộ lọc: " + ex.getMessage());
        }
    }

    private void exportCsv() {
        if (lastFiltered == null || lastFiltered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất file.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất danh sách đã lọc ra CSV");
        int r = chooser.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) path = path + ".csv";

        try {
            final String finalPath = path;
            exportService.exportToCSV(lastFiltered, finalPath, apt -> apartmentService.getAmenitiesForApartment(apt.getId()));
            JOptionPane.showMessageDialog(this, "Xuất file thành công!\n" + finalPath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất CSV: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}