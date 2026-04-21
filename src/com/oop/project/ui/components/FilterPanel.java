package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import com.oop.project.service.ApartmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * FilterPanel — FR-2.1, FR-2.2, FR-2.3, FR-2.4
 * Structured layout:
 *   NORTH  → compact criteria bar (always fully visible)
 *   CENTER → sortable result table
 *   SOUTH  → View Detail button
 */
public class FilterPanel extends JPanel {

    private final ApartmentService service;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Filter widgets
    private JTextField    txtSearch, txtPriceFrom, txtPriceTo;
    private JSpinner      spnMinBedrooms;
    private JComboBox<String> cbLocation, cbCategory;
    private JCheckBox[] amenityItems;
    private JButton btnAmenitiesPopup;

    // ── Amenity names list (extensible) ──────────────────────────────────────
    private static final String[] AMENITIES = {
        "WiFi", "Swimming Pool", "Gym", "Parking", "Security 24/7",
        "Balcony", "Elevator", "Air Conditioning", "BBQ Area", "Playground"
    };

    private static final String[] PROVINCES = {
        "All Locations", "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
        "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh",
        "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau",
        "Cao Bằng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp",
        "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang",
        "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
        "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An",
        "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam",
        "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh",
        "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang",
        "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    };

    public FilterPanel(ApartmentService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(18, 18, 18, 18));
        setBackground(Color.WHITE);
        initComponents();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void initComponents() {

        // ── NORTH: Criteria panel (fixed height, never wraps) ──────────────
        JPanel pnlCriteria = buildCriteriaPanel();

        // ── CENTER: Sortable result table ───────────────────────────────────
        String[] columns = {"ID", "Code", "Address", "Location", "Price ($)", "Bedrooms", "Area (m²)", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        JScrollPane scrollPane = new JScrollPane(table);

        // ── SOUTH: View detail button ────────────────────────────────────────
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnDetail = styledBtn("View Selected Detail", new Color(52, 152, 219));
        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment first!"); return; }
            int modelRow = table.convertRowIndexToModel(row);
            int aptId = (int) tableModel.getValueAt(modelRow, 0);
            List<String> amenities = service.getAmenitiesForApartment(aptId);
            new com.oop.project.ui.panels.ApartmentDetailDialog(
                SwingUtilities.getWindowAncestor(this), service.getById(aptId), amenities).setVisible(true);
        });
        pnlFooter.add(btnDetail);

        add(pnlCriteria, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(pnlFooter,   BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Build the filter criteria area (3 rows, always visible)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCriteriaPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBackground(new Color(248, 249, 251));
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        // ── Row 1: Keyword | Location | Category | Min Beds ─────────────────
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.setOpaque(false);

        txtSearch = new JTextField(13);
        cbLocation = new JComboBox<>(PROVINCES);
        cbCategory = new JComboBox<>(new String[]{"All Types", "Luxury", "Standard", "Budget"});
        spnMinBedrooms = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        ((JSpinner.DefaultEditor) spnMinBedrooms.getEditor()).getTextField().setColumns(3);

        row1.add(label("Keyword:"));    row1.add(txtSearch);
        row1.add(label("Location:"));   row1.add(cbLocation);
        row1.add(label("Category:"));   row1.add(cbCategory);
        row1.add(label("Min Beds:"));   row1.add(spnMinBedrooms);

        // ── Row 2: Price range | Amenities (scrollable box) | Buttons ────────
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setOpaque(false);

        txtPriceFrom = new JTextField(6);
        txtPriceTo   = new JTextField(6);

        // Amenities — popup button that reveals checklist when clicked
        JPopupMenu amenityPopup = new JPopupMenu();
        JPanel pnlTicks = new JPanel(new GridLayout(0, 1, 0, 2));
        pnlTicks.setBackground(Color.WHITE);
        pnlTicks.setBorder(new EmptyBorder(5, 5, 5, 5));
        amenityItems = new JCheckBox[AMENITIES.length];
        for (int i = 0; i < AMENITIES.length; i++) {
            amenityItems[i] = new JCheckBox(AMENITIES[i]);
            amenityItems[i].setBackground(Color.WHITE);
            amenityItems[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            amenityItems[i].setFocusPainted(false);
            final JCheckBox item = amenityItems[i];
            item.addActionListener(e -> {
                updateAmenityButtonLabel();
                executeFilter();
            });
            pnlTicks.add(amenityItems[i]);
        }
        amenityPopup.add(pnlTicks);
        
        // Add Clear All option at bottom
        amenityPopup.addSeparator();
        JMenuItem clearItem = new JMenuItem("Clear All");
        clearItem.addActionListener(e -> {
            for (JCheckBox m : amenityItems) m.setSelected(false);
            updateAmenityButtonLabel();
            executeFilter();
        });
        amenityPopup.add(clearItem);

        btnAmenitiesPopup = styledBtn("Amenities (Any)", new Color(52, 73, 94));
        btnAmenitiesPopup.addActionListener(e ->
            amenityPopup.show(btnAmenitiesPopup, 0, btnAmenitiesPopup.getHeight()));

        JButton btnApply = styledBtn("Apply Filter", new Color(44, 62, 80));
        JButton btnReset = styledBtn("Reset",        new Color(149, 165, 166));
        btnApply.addActionListener(e -> executeFilter());
        btnReset.addActionListener(e -> resetFilters());

        row2.add(label("Price $:"));   row2.add(txtPriceFrom);
        row2.add(label("-"));           row2.add(txtPriceTo);
        row2.add(btnAmenitiesPopup);
        row2.add(Box.createHorizontalStrut(6));
        row2.add(btnApply);
        row2.add(btnReset);

        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 4));
        rows.setOpaque(false);
        rows.add(row1);
        rows.add(row2);
        outer.add(rows, BorderLayout.CENTER);

        // ── Live update listeners ─────────────────────────────────────────────
        DocumentListener live = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { executeFilter(); }
            public void removeUpdate(DocumentEvent e)  { executeFilter(); }
            public void changedUpdate(DocumentEvent e) {}
        };
        txtSearch   .getDocument().addDocumentListener(live);
        txtPriceFrom.getDocument().addDocumentListener(live);
        txtPriceTo  .getDocument().addDocumentListener(live);
        cbLocation    .addActionListener(e -> executeFilter());
        cbCategory    .addActionListener(e -> executeFilter());
        spnMinBedrooms.addChangeListener(e  -> executeFilter());

        return outer;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Filter logic
    // ─────────────────────────────────────────────────────────────────────────
    private String normalize(String s) {
        if (s == null) return "";
        String r = Normalizer.normalize(s, Normalizer.Form.NFD);
        r = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(r).replaceAll("").toLowerCase();
        return r.replace("tp. ", "").replace("tp.", "").replace("tp ", "")
                .replace("hcm", "ho chi minh").replace("hn", "ha noi").replace("dn", "da nang").trim();
    }

    private void executeFilter() {
        String keyword    = normalize(txtSearch.getText());
        String locFilter  = normalize(cbLocation.getSelectedItem().toString());
        String typeFilter = cbCategory.getSelectedItem().toString();   // e.g. "Luxury"
        int    minBeds    = (int) spnMinBedrooms.getValue();

        Double minPrice = null, maxPrice = null;
        try {
            if (!txtPriceFrom.getText().trim().isEmpty()) minPrice = Double.parseDouble(txtPriceFrom.getText().trim());
            if (!txtPriceTo.getText().trim().isEmpty())   maxPrice = Double.parseDouble(txtPriceTo.getText().trim());
        } catch (NumberFormatException ex) { return; }

        List<String> selectedAmenities = new ArrayList<>();
        for (JCheckBox item : amenityItems) {
            if (item.isSelected()) selectedAmenities.add(item.getText());
        }

        List<Apartment> all = service.findAll();
        tableModel.setRowCount(0);

        for (Apartment a : all) {
            // Keyword: code + address + location
            boolean mKey = keyword.isEmpty()
                || normalize(a.getListingCode()).contains(keyword)
                || normalize(a.getAddress()).contains(keyword)
                || normalize(a.getLocation()).contains(keyword);

            // Location
            boolean mLoc = locFilter.equals("all locations")
                || normalize(a.getLocation()).contains(locFilter);

            // Category — compare against model's auto-computed category (FR-4.3)
            boolean mType = typeFilter.equals("All Types")
                || a.getCategory().equalsIgnoreCase(typeFilter);

            // Price range
            boolean mPrice = true;
            if (minPrice != null && a.getPrice() < minPrice) mPrice = false;
            if (maxPrice != null && a.getPrice() > maxPrice) mPrice = false;

            // Min bedrooms (FR-2.1)
            boolean mBeds = (minBeds == 0) || (a.getBedrooms() >= minBeds);

            // Amenities — AND logic: apartment must have ALL selected (FR-2.2)
            boolean mAmen = true;
            if (!selectedAmenities.isEmpty()) {
                List<String> aptAmenities = service.getAmenitiesForApartment(a.getId());
                mAmen = aptAmenities.containsAll(selectedAmenities);
            }

            if (mKey && mLoc && mType && mPrice && mBeds && mAmen) {
                tableModel.addRow(new Object[]{
                    a.getId(), a.getListingCode(), a.getAddress(), a.getLocation(),
                    a.getPrice(), a.getBedrooms(), a.getArea(), a.getCategory()
                });
            }
        }
    }

    private void resetFilters() {
        txtSearch.setText("");
        cbLocation.setSelectedIndex(0);
        cbCategory.setSelectedIndex(0);
        txtPriceFrom.setText("");
        txtPriceTo.setText("");
        spnMinBedrooms.setValue(0);
        for (JCheckBox item : amenityItems) item.setSelected(false);
        updateAmenityButtonLabel();
        tableModel.setRowCount(0);
    }

    private void updateAmenityButtonLabel() {
        long count = 0;
        for (JCheckBox item : amenityItems) if (item.isSelected()) count++;
        btnAmenitiesPopup.setText(count == 0 ? "Amenities (Any)" : "Amenities (" + count + " selected)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }
}