package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.model.User;
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

public class ListingPanel extends JPanel {
    private final ApartmentService service;
    private final User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch, txtPriceFrom, txtPriceTo;
    private JComboBox<String> cbCategory, cbFilterLocation;

    public static final String[] PROVINCES_FORM = {
        "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ", "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    };
    public static final String[] AMENITIES = {
        "WiFi", "Swimming Pool", "Gym", "Parking", "Security 24/7", "Balcony", "Elevator", "Air Conditioning", "BBQ Area", "Playground"
    };

    public ListingPanel(ApartmentService service, User user) {
        this.service = service;
        this.currentUser = user;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        initComponents();
        loadData();
    }

    private void initComponents() {
        // ========================================================
        // 1. THANH TÌM KIẾM & BỘ LỌC
        // ========================================================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        filterPanel.setOpaque(false);

        txtSearch = new JTextField(12);
        
        String[] provincesFilter = new String[PROVINCES_FORM.length + 1];
        provincesFilter[0] = "All Locations";
        System.arraycopy(PROVINCES_FORM, 0, provincesFilter, 1, PROVINCES_FORM.length);
        cbFilterLocation = new JComboBox<>(provincesFilter);
        
        txtPriceFrom = new JTextField(5);
        txtPriceTo = new JTextField(5);
        cbCategory = new JComboBox<>(new String[]{"All Types", "LUXURY", "STANDARD", "BUDGET"});
        
        JButton btnFilter = new JButton("Search & Filter");
        btnFilter.setBackground(new Color(44, 62, 80));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setFocusPainted(false);
        btnFilter.addActionListener(e -> executeFilter());

        JButton btnReset = new JButton("Reset");
        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> resetFilters());

        filterPanel.add(new JLabel("Search:")); filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("| Location:")); filterPanel.add(cbFilterLocation);
        filterPanel.add(new JLabel("| Price:")); filterPanel.add(txtPriceFrom);
        filterPanel.add(new JLabel("-")); filterPanel.add(txtPriceTo);
        filterPanel.add(new JLabel("| Type:")); filterPanel.add(cbCategory);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        // ========================================================
        // 2. BẢNG DỮ LIỆU
        // ========================================================
        String[] columns = {"ID", "Code", "Address", "Location", "Price", "Area"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);

        // ========================================================
        // 3. THANH NÚT BẤM (NÚT DELETE CÁCH LY)
        // ========================================================
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = createBtn("Add New", new Color(46, 204, 113));
        JButton btnEdit = createBtn("Edit", new Color(241, 196, 15));
        JButton btnDetail = createBtn("Detail", new Color(52, 152, 219));
        JButton btnFav = createBtn("Favorite", new Color(155, 89, 182));
        JButton btnDelete = createBtn("Delete", new Color(231, 76, 60));

        actionPanel.add(btnAdd); actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(btnEdit); actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(btnDetail); actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(btnFav);
        
        // Đẩy Delete văng sang bên phải
        actionPanel.add(Box.createHorizontalGlue()); 
        actionPanel.add(btnDelete);

        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        btnDetail.addActionListener(e -> handleShowDetail());
        btnFav.addActionListener(e -> handleFavorite());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.add(actionPanel, BorderLayout.SOUTH);

        add(filterPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    // ========================================================
    // LOGIC LỌC DỮ LIỆU ĐỈNH CAO CHẤP MỌI LOẠI TIẾNG VIỆT
    // ========================================================
    private void resetFilters() {
        txtSearch.setText("");
        cbFilterLocation.setSelectedIndex(0);
        txtPriceFrom.setText("");
        txtPriceTo.setText("");
        cbCategory.setSelectedIndex(0);
        loadData();
    }

    private String normalizeString(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(temp).replaceAll("").toLowerCase().trim();
        
        // Gọt sạch sẽ tiền tố lằng nhằng
        result = result.replace("thanh pho ", "")
                       .replace("thanh pho", "")
                       .replace("tp. ", "")
                       .replace("tp.", "")
                       .replace("tp ", ""); 
                       
        // Phiên dịch từ viết tắt
        result = result.replace("hcm", "ho chi minh")
                       .replace("hn", "ha noi")
                       .replace("dn", "da nang");
                       
        return result.replaceAll("\\s+", " ").trim();
    }

    private void executeFilter() {
        String keyword = normalizeString(txtSearch.getText());
        String location = cbFilterLocation.getSelectedItem().toString();
        String type = cbCategory.getSelectedItem().toString();
        
        Double minPrice = null, maxPrice = null;
        try {
            if (!txtPriceFrom.getText().trim().isEmpty()) minPrice = Double.parseDouble(txtPriceFrom.getText().trim());
            if (!txtPriceTo.getText().trim().isEmpty()) maxPrice = Double.parseDouble(txtPriceTo.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Apartment> allApts = service.findAll();
        List<Apartment> filteredList = new ArrayList<>();

        for (Apartment a : allApts) {
            // Lọc Keyword: Quét trên cả Code, Address và Location
            boolean matchKeyword = true;
            if (!keyword.isEmpty()) {
                String dbCode = normalizeString(a.getListingCode());
                String dbAddr = normalizeString(a.getAddress());
                String dbLoc = normalizeString(a.getLocation()); 
                // Chỉ cần 1 trong 3 cái chứa keyword là lụm
                matchKeyword = dbCode.contains(keyword) || dbAddr.contains(keyword) || dbLoc.contains(keyword);
            }
            
            // Lọc Location theo ComboBox
            boolean matchLocation = true;
            if (!location.equals("All Locations")) {
                String dbLoc = normalizeString(a.getLocation());
                String filterLoc = normalizeString(location);
                matchLocation = dbLoc.contains(filterLoc);
            }
                
            boolean matchType = true;
            if (!type.equals("All Types")) {
                String dbType = a.getType() != null ? a.getType().name() : "";
                matchType = dbType.equalsIgnoreCase(type);
            }
                
            boolean matchPrice = true;
            if (minPrice != null && a.getPrice() < minPrice) matchPrice = false;
            if (maxPrice != null && a.getPrice() > maxPrice) matchPrice = false;

            if (matchKeyword && matchLocation && matchType && matchPrice) {
                filteredList.add(a);
            }
        }
        populateTable(filteredList);
    }

    private void populateTable(List<Apartment> list) {
        tableModel.setRowCount(0);
        if (list != null) {
            for (Apartment a : list) {
                tableModel.addRow(new Object[]{ a.getId(), a.getListingCode(), a.getAddress(), a.getLocation(), "$" + a.getPrice(), a.getArea() + " m²" });
            }
        }
    }

    private void loadData() {
        populateTable(service.findAll()); 
    }

    // ========================================================
    // FORM ADD / EDIT (CÓ DROPDOWN LOCATION & CHECKBOX)
    // ========================================================
    private void handleAdd() {
        Apartment newApt = showApartmentForm("Add New Apartment", null);
        if (newApt != null) {
            try { service.save(newApt); loadData(); JOptionPane.showMessageDialog(this, "Added!"); } 
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment!"); return; }
        Apartment existingApt = service.getById((int) table.getValueAt(row, 0));
        
        if (existingApt != null) {
            Apartment updatedApt = showApartmentForm("Edit Apartment", existingApt);
            if (updatedApt != null) {
                try { service.update(updatedApt); loadData(); JOptionPane.showMessageDialog(this, "Updated!"); } 
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment to delete!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this apartment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { service.delete((int) table.getValueAt(row, 0)); loadData(); JOptionPane.showMessageDialog(this, "Deleted!"); } 
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private Apartment showApartmentForm(String title, Apartment apt) {
        JTextField txtCode = new JTextField(apt != null ? apt.getListingCode() : "");
        JTextField txtAddress = new JTextField(apt != null ? apt.getAddress() : "");
        JTextField txtPrice = new JTextField(apt != null ? String.valueOf(apt.getPrice()) : "");
        JTextField txtBedrooms = new JTextField(apt != null ? String.valueOf(apt.getBedrooms()) : "");
        JTextField txtArea = new JTextField(apt != null ? String.valueOf(apt.getArea()) : "");
        
        JComboBox<String> cbLocation = new JComboBox<>(PROVINCES_FORM);
        if (apt != null && apt.getLocation() != null) { cbLocation.setSelectedItem(apt.getLocation()); }

        JComboBox<String> cbType = new JComboBox<>(new String[]{"LUXURY", "STANDARD", "BUDGET"});
        if (apt != null && apt.getType() != null) { cbType.setSelectedItem(apt.getType().name()); }

        JPanel pnlInfo = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlInfo.add(new JLabel("Listing Code:")); pnlInfo.add(txtCode);
        pnlInfo.add(new JLabel("Address:")); pnlInfo.add(txtAddress);
        pnlInfo.add(new JLabel("Location (City):")); pnlInfo.add(cbLocation);
        pnlInfo.add(new JLabel("Price ($):")); pnlInfo.add(txtPrice);
        pnlInfo.add(new JLabel("Bedrooms:")); pnlInfo.add(txtBedrooms);
        pnlInfo.add(new JLabel("Area (m²):")); pnlInfo.add(txtArea); 
        pnlInfo.add(new JLabel("Category:")); pnlInfo.add(cbType);

        JPanel pnlAmenities = new JPanel(new GridLayout(0, 3, 5, 5));
        pnlAmenities.setBorder(BorderFactory.createTitledBorder(new EmptyBorder(10,0,0,0), "Select Amenities", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        for (String am : AMENITIES) { pnlAmenities.add(new JCheckBox(am)); }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(pnlInfo, BorderLayout.CENTER);
        mainPanel.add(pnlAmenities, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, mainPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                return new Apartment(apt != null ? apt.getId() : 0, txtCode.getText(), txtAddress.getText(),
                    cbLocation.getSelectedItem().toString(), Double.parseDouble(txtPrice.getText()), 
                    Integer.parseInt(txtBedrooms.getText()), Integer.parseInt(txtArea.getText()), 
                    ApartmentType.valueOf(cbType.getSelectedItem().toString()), apt != null ? apt.getCreatedBy() : currentUser.getId()
                );
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid number format!"); }
        }
        return null; 
    }

    private void handleShowDetail() {
        int row = table.getSelectedRow();
        if (row != -1) { new ApartmentDetailDialog(null, service.getById((int) table.getValueAt(row, 0))).setVisible(true); }
    }

    private void handleFavorite() {
        int row = table.getSelectedRow();
        if (row != -1) { service.toggleFavorite(currentUser.getId(), (int) table.getValueAt(row, 0)); JOptionPane.showMessageDialog(this, "Favorite Toggled!"); }
    }
}