package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.ApartmentDetailDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FilterPanel extends JPanel {

    private final ApartmentService service;
    private JTable table;
    private DefaultTableModel tableModel;

    // Các thành phần bộ lọc
    private JTextField txtSearch, txtPriceFrom, txtPriceTo;
    private JComboBox<String> cbLocation, cbCategory;

    // DATA TỈNH THÀNH NHÚNG THẲNG
    private final String[] PROVINCES = {
        "All Locations", "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ", "An Giang", 
        "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", 
        "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cao Bằng", 
        "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", 
        "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình", 
        "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng", 
        "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình", 
        "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", 
        "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", 
        "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", 
        "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    };

    public FilterPanel(ApartmentService service) {
        this.service = service;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        initComponents();
    }

    private void initComponents() {
        // --- 1. KHU VỰC NHẬP LIỆU LỌC (TOP) ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createTitledBorder(
            new EmptyBorder(5,0,5,0), "Filter Criteria", TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), new Color(44, 62, 80)));

        txtSearch = new JTextField(10);
        cbLocation = new JComboBox<>(PROVINCES);
        txtPriceFrom = new JTextField(5);
        txtPriceTo = new JTextField(5);
        cbCategory = new JComboBox<>(new String[]{"All Types", "LUXURY", "STANDARD", "BUDGET"});

        JButton btnSearch = new JButton("Apply Filter");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> executeFilter());

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> resetFilters());

        pnlHeader.add(new JLabel("Keyword:")); pnlHeader.add(txtSearch);
        pnlHeader.add(new JLabel("Location:")); pnlHeader.add(cbLocation);
        pnlHeader.add(new JLabel("Price:")); pnlHeader.add(txtPriceFrom);
        pnlHeader.add(new JLabel("-")); pnlHeader.add(txtPriceTo);
        pnlHeader.add(new JLabel("Type:")); pnlHeader.add(cbCategory);
        pnlHeader.add(btnSearch); pnlHeader.add(btnReset);

        // --- 2. BẢNG HIỂN THỊ KẾT QUẢ (CENTER) ---
        String[] columns = {"ID", "Code", "Address", "Location", "Price", "Area"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);

        // --- 3. NÚT XEM CHI TIẾT (BOTTOM) ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnDetail = new JButton("View Selected Detail");
        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) new ApartmentDetailDialog(null, service.getById((int) table.getValueAt(row, 0))).setVisible(true);
        });
        pnlFooter.add(btnDetail);

        add(pnlHeader, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // Công cụ "tẩy trắng" tiếng Việt chấp mọi loại data cũ/mới
    private String normalize(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        String result = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(temp).replaceAll("").toLowerCase();
        return result.replace("tp. ", "").replace("tp.", "").replace("hcm", "ho chi minh")
                     .replace("hn", "ha noi").replace("dn", "da nang").trim();
    }

    private void executeFilter() {
        String keyword = normalize(txtSearch.getText());
        String locFilter = normalize(cbLocation.getSelectedItem().toString());
        String typeFilter = cbCategory.getSelectedItem().toString();

        List<Apartment> all = service.findAll();
        tableModel.setRowCount(0); // Xóa bảng trước khi hiện kết quả mới

        for (Apartment a : all) {
            boolean mKey = keyword.isEmpty() || normalize(a.getListingCode()).contains(keyword) || normalize(a.getAddress()).contains(keyword);
            boolean mLoc = locFilter.equals("all locations") || normalize(a.getLocation()).contains(locFilter);
            boolean mType = typeFilter.equals("All Types") || a.getType().name().equalsIgnoreCase(typeFilter);
            
            if (mKey && mLoc && mType) {
                tableModel.addRow(new Object[]{ a.getId(), a.getListingCode(), a.getAddress(), a.getLocation(), "$" + a.getPrice(), a.getArea() + " m²" });
            }
        }
        if(tableModel.getRowCount() == 0) JOptionPane.showMessageDialog(this, "Không tìm thấy căn hộ nào!");
    }

    private void resetFilters() {
        txtSearch.setText(""); cbLocation.setSelectedIndex(0);
        txtPriceFrom.setText(""); txtPriceTo.setText("");
        cbCategory.setSelectedIndex(0); tableModel.setRowCount(0);
    }
}