package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.model.Role;
import com.oop.project.model.User;
import com.oop.project.repository.NoteRepository;
import com.oop.project.service.ApartmentService;
import com.oop.project.service.ExportService;

import com.oop.project.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ListingPanel extends JPanel {
    private final ApartmentService service;
    private final User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter; 

    // Search bar UI
    private JTextField txtSearch;

    // Advanced Filter state
    private AdvancedFilterCriteria currentCriteria = new AdvancedFilterCriteria();

    public static final String[] PROVINCES_FORM = {
        "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ", "An Giang", "Bà Rịa - Vũng Tàu",
        "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương",
        "Bình Phước", "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông", "Điện Biên",
        "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương",
        "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
        "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình",
        "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh",
        "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
        "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    };
    public static final String[] AMENITIES = {
        "WiFi", "Swimming Pool", "Gym", "Parking", "Security 24/7",
        "Balcony", "Elevator", "Air Conditioning", "BBQ Area", "Playground"
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
        // ── Top Search Bar ───────────────────────────────────────────────────
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Enter keyword, listing code, address...");

        JButton btnAdvanced = new JButton("Advanced Search");
        btnAdvanced.setBackground(new Color(44, 62, 80));
        btnAdvanced.setForeground(Color.WHITE);
        btnAdvanced.setFocusPainted(false);
        btnAdvanced.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdvanced.addActionListener(e -> showAdvancedSearchDialog());

        JButton btnClear = new JButton("Clear Filters");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClear.addActionListener(e -> resetFilters());

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(txtSearch);
        filterPanel.add(btnAdvanced);
        filterPanel.add(btnClear);

        // FR-2.3: Live search on keyword field
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { executeFilter(); }
            public void removeUpdate(DocumentEvent e)  { executeFilter(); }
            public void changedUpdate(DocumentEvent e) { executeFilter(); }
        });

        // ── Table with RowSorter (FR-2.4 / FR-5.1) ───────────────────────────
        String[] columns = {"ID", "Code", "Address", "Location", "Price ($)", "Bedrooms", "Area (m²)", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(table);

        // ── Action buttons ────────────────────────────────────────────────────
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd    = createBtn("Add New",  new Color(46, 204, 113));
        JButton btnRefresh= createBtn("Refresh",    new Color(149, 165, 166)); 
        
        // ── Option Popup Menu ──
        JPopupMenu optionMenu = new JPopupMenu();
        
        JMenuItem itemEdit   = new JMenuItem("Edit");
        JMenuItem itemDetail = new JMenuItem("Detail");
        JMenuItem itemFav    = new JMenuItem("Favorite");
        JMenuItem itemNotes  = new JMenuItem("Notes");
        JMenuItem itemExport = new JMenuItem("Export CSV");
        JMenuItem itemDelete = new JMenuItem("Delete");

        itemEdit.addActionListener(e -> handleEdit());
        itemDetail.addActionListener(e -> handleShowDetail());
        itemFav.addActionListener(e -> handleFavorite());
        itemNotes.addActionListener(e -> handleNotes());
        itemExport.addActionListener(e -> handleExportCSV());
        itemDelete.addActionListener(e -> handleDelete());

        itemDelete.setForeground(new Color(231, 76, 60)); // Red text for delete

        optionMenu.add(itemEdit);
        optionMenu.add(itemDetail);
        optionMenu.add(itemFav);
        optionMenu.add(itemNotes);
        optionMenu.addSeparator();
        optionMenu.add(itemExport);

        if (currentUser.getRole() == Role.ADMIN) {
            optionMenu.addSeparator();
            optionMenu.add(itemDelete);
        }

        JButton btnOption = createBtn("Options", new Color(52, 152, 219));
        btnOption.addActionListener(e -> {
            optionMenu.show(btnOption, 0, btnOption.getHeight());
        });

        btnAdd.addActionListener(e -> handleAdd());
        btnRefresh.addActionListener(e -> resetFilters());

        actionPanel.add(btnAdd);    actionPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        actionPanel.add(btnOption); actionPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        actionPanel.add(btnRefresh);
        actionPanel.add(Box.createHorizontalGlue());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.add(actionPanel, BorderLayout.SOUTH);

        add(filterPanel,   BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    // ── Filter logic ─────────────────────────────────────────────────────────

    private static class AdvancedFilterCriteria {
        String location = "All Locations";
        String category = "All Types";
        Double minPrice = null;
        Double maxPrice = null;
        int minBedrooms = 0;
        List<String> amenities = new ArrayList<>();
    }

    private void resetFilters() {
        txtSearch.setText("");
        currentCriteria = new AdvancedFilterCriteria();
        executeFilter();
    }

    private String normalizeString(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        String result = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(temp).replaceAll("").toLowerCase().trim();
        result = result.replace("thanh pho ", "").replace("thanh pho", "").replace("tp. ", "").replace("tp.", "").replace("tp ", "");
        result = result.replace("hcm", "ho chi minh").replace("hn", "ha noi").replace("dn", "da nang");
        return result.replaceAll("\\s+", " ").trim();
    }

    private void executeFilter() {
        String keyword  = normalizeString(txtSearch.getText());

        List<Apartment> filteredList = new ArrayList<>();
        for (Apartment a : service.findAll()) {
            boolean matchKeyword = true;
            if (!keyword.isEmpty()) {
                String dbCode = normalizeString(a.getListingCode());
                String dbAddr = normalizeString(a.getAddress());
                String dbLoc  = normalizeString(a.getLocation());
                
                List<String> amenities = service.getAmenitiesForApartment(a.getId());
                String amenText = normalizeString(String.join(" ", amenities));
                String dbDesc = normalizeString(a.getDescription());
                matchKeyword = dbCode.contains(keyword) || dbAddr.contains(keyword)
                            || dbLoc.contains(keyword)  || amenText.contains(keyword)
                            || dbDesc.contains(keyword);
            }
            boolean matchLocation = currentCriteria.location.equals("All Locations") ||
                normalizeString(a.getLocation()).contains(normalizeString(currentCriteria.location));
            
            boolean matchType = currentCriteria.category.equals("All Types") ||
                (a.getCategory() != null && a.getCategory().equalsIgnoreCase(currentCriteria.category));
            
            boolean matchPrice = true;
            if (currentCriteria.minPrice != null && a.getPrice() < currentCriteria.minPrice) matchPrice = false;
            if (currentCriteria.maxPrice != null && a.getPrice() > currentCriteria.maxPrice) matchPrice = false;

            boolean matchBedrooms = currentCriteria.minBedrooms == 0 || a.getBedrooms() >= currentCriteria.minBedrooms;

            boolean matchAmenities = true;
            if (!currentCriteria.amenities.isEmpty()) {
                List<String> aptAmenities = service.getAmenitiesForApartment(a.getId());
                matchAmenities = aptAmenities.containsAll(currentCriteria.amenities);
            }

            if (matchKeyword && matchLocation && matchType && matchPrice && matchBedrooms && matchAmenities) {
                filteredList.add(a);
            }
        }
        populateTable(filteredList);
    }

    private void populateTable(List<Apartment> list) {
        tableModel.setRowCount(0);
        if (list != null) {
            for (Apartment a : list) {
                tableModel.addRow(new Object[]{
                    a.getId(), a.getListingCode(), a.getAddress(), a.getLocation(),
                    a.getPrice(), a.getBedrooms(), a.getArea(), a.getCategory()
                });
            }
        }
    }

    private void loadData() { populateTable(service.findAll()); }

    // ── Advanced Search Dialog ────────────────────────────────────────────────
    private void showAdvancedSearchDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Advanced Search", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        
        JPanel pnlMain = new JPanel(new GridLayout(0, 2, 10, 15));
        pnlMain.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Location
        String[] locations = new String[PROVINCES_FORM.length + 1];
        locations[0] = "All Locations";
        System.arraycopy(PROVINCES_FORM, 0, locations, 1, PROVINCES_FORM.length);
        JComboBox<String> cbLoc = new JComboBox<>(locations);
        cbLoc.setSelectedItem(currentCriteria.location);

        // Category
        JComboBox<String> cbCat = new JComboBox<>(new String[]{"All Types", "Luxury", "Standard", "Budget"});
        cbCat.setSelectedItem(currentCriteria.category);

        // Min Beds
        JSpinner spinBeds = new JSpinner(new SpinnerNumberModel(currentCriteria.minBedrooms, 0, 10, 1));

        // Price String values
        JTextField txtMinPrice = new JTextField(currentCriteria.minPrice != null ? String.valueOf(currentCriteria.minPrice) : "");
        JTextField txtMaxPrice = new JTextField(currentCriteria.maxPrice != null ? String.valueOf(currentCriteria.maxPrice) : "");

        pnlMain.add(new JLabel("Location:"));    pnlMain.add(cbLoc);
        pnlMain.add(new JLabel("Category:"));    pnlMain.add(cbCat);
        pnlMain.add(new JLabel("Min Bedrooms:"));pnlMain.add(spinBeds);
        pnlMain.add(new JLabel("Min Price ($):")); pnlMain.add(txtMinPrice);
        pnlMain.add(new JLabel("Max Price ($):")); pnlMain.add(txtMaxPrice);

        // Amenities
        JPanel pnlAmenities = new JPanel(new GridLayout(0, 2, 5, 5));
        pnlAmenities.setBackground(Color.WHITE);
        JCheckBox[] chkAmenities = new JCheckBox[AMENITIES.length];
        for (int i = 0; i < AMENITIES.length; i++) {
            chkAmenities[i] = new JCheckBox(AMENITIES[i]);
            chkAmenities[i].setBackground(Color.WHITE);
            chkAmenities[i].setSelected(currentCriteria.amenities.contains(AMENITIES[i]));
            pnlAmenities.add(chkAmenities[i]);
        }
        
        JScrollPane scrollAmenities = new JScrollPane(pnlAmenities);
        scrollAmenities.setPreferredSize(new Dimension(280, 120));
        scrollAmenities.setBorder(BorderFactory.createTitledBorder("Must contain Amenities:"));

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(pnlMain, BorderLayout.NORTH);
        centerContainer.add(scrollAmenities, BorderLayout.CENTER);
        centerContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApply = new JButton("Apply Filters");
        btnApply.setBackground(new Color(52, 152, 219));
        btnApply.setForeground(Color.WHITE);
        btnApply.addActionListener(e -> {
            try {
                currentCriteria.location = cbLoc.getSelectedItem().toString();
                currentCriteria.category = cbCat.getSelectedItem().toString();
                currentCriteria.minBedrooms = (int) spinBeds.getValue();
                
                String minP = txtMinPrice.getText().trim();
                currentCriteria.minPrice = minP.isEmpty() ? null : Double.parseDouble(minP);
                
                String maxP = txtMaxPrice.getText().trim();
                currentCriteria.maxPrice = maxP.isEmpty() ? null : Double.parseDouble(maxP);
                
                currentCriteria.amenities.clear();
                for (JCheckBox chk : chkAmenities) {
                    if (chk.isSelected()) currentCriteria.amenities.add(chk.getText());
                }
                
                executeFilter();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid price format!");
            }
        });
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());
        pnlButtons.add(btnApply);
        pnlButtons.add(btnCancel);

        dialog.add(centerContainer, BorderLayout.CENTER);
        dialog.add(pnlButtons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ── CRUD handlers ─────────────────────────────────────────────────────────
    private void handleAdd() {
        Object[] result = showApartmentForm("Add New Apartment", null);
        if (result != null) {
            Apartment newApt = (Apartment) result[0];
            @SuppressWarnings("unchecked") List<String> selectedAmenities = (List<String>) result[1];
            try {
                service.save(newApt);
                if (!selectedAmenities.isEmpty()) service.saveAmenities(newApt.getId(), selectedAmenities);
                writeAuditLog(newApt.getId(), "CREATE", "Added: " + newApt.getListingCode());
                loadData();
                JOptionPane.showMessageDialog(this, "Apartment added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment to edit!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        Apartment existing = service.getById((int) tableModel.getValueAt(modelRow, 0));
        if (existing != null) {
            Object[] result = showApartmentForm("Edit Apartment", existing);
            if (result != null) {
                Apartment updated = (Apartment) result[0];
                @SuppressWarnings("unchecked") List<String> selectedAmenities = (List<String>) result[1];
                try {
                    service.update(updated);
                    service.saveAmenities(updated.getId(), selectedAmenities);
                    writeAuditLog(updated.getId(), "UPDATE", "Updated: " + updated.getListingCode());
                    loadData();
                    JOptionPane.showMessageDialog(this, "Updated!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment to delete!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int aptId = (int) tableModel.getValueAt(modelRow, 0);
        String code = tableModel.getValueAt(modelRow, 1).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete this apartment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                writeAuditLog(aptId, "DELETE", "Deleted: " + code);
                service.delete(aptId);
                loadData();
                JOptionPane.showMessageDialog(this, "Deleted!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleShowDetail() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int aptId = (int) tableModel.getValueAt(modelRow, 0);
        Apartment apt = service.getById(aptId);
        List<String> amenities = service.getAmenitiesForApartment(aptId);
        NoteRepository noteRepo = new NoteRepository();
        new ApartmentDetailDialog(
            SwingUtilities.getWindowAncestor(this), apt, amenities, noteRepo, aptId).setVisible(true);
    }

    private void handleFavorite() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        service.toggleFavorite(currentUser.getId(), (int) tableModel.getValueAt(modelRow, 0));
        JOptionPane.showMessageDialog(this, "Favorite toggled!");
    }

    /** FR-3.2 / FR-3.4: Open NotesDialog for the selected apartment */
    private void handleNotes() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an apartment!"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int aptId = (int) tableModel.getValueAt(modelRow, 0);
        NoteRepository noteRepo = new NoteRepository();
        new NotesDialog(SwingUtilities.getWindowAncestor(this), noteRepo, aptId, currentUser.getId())
            .setVisible(true);
    }

    /** FR-4.1: Export currently visible/filtered rows to CSV */
    private void handleExportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export listings to CSV");
        chooser.setSelectedFile(new File("listings_export.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        // Collect apartments matching the current filtered rows
        List<Apartment> toExport = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelRow = table.convertRowIndexToModel(i);
            int aptId = (int) tableModel.getValueAt(modelRow, 0);
            Apartment apt = service.getById(aptId);
            if (apt != null) toExport.add(apt);
        }

        try {
            ExportService exporter = new ExportService();
            exporter.exportToCSV(toExport, chooser.getSelectedFile().getAbsolutePath(),
                apt -> service.getAmenitiesForApartment(apt.getId()));
            JOptionPane.showMessageDialog(this,
                "Exported " + toExport.size() + " listings to:\n" + chooser.getSelectedFile().getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Add / Edit Form ───────────────────────────────────────────────────────
    /**
     * Returns Object[]{Apartment, List<String> amenities} or null if cancelled.
     */
    private Object[] showApartmentForm(String title, Apartment apt) {
        JTextField txtCode     = new JTextField(apt != null ? apt.getListingCode() : "");
        JTextField txtAddress  = new JTextField(apt != null ? apt.getAddress() : "");
        JTextField txtPrice    = new JTextField(apt != null ? String.valueOf(apt.getPrice()) : "");
        JTextField txtBedrooms = new JTextField(apt != null ? String.valueOf(apt.getBedrooms()) : "");
        JTextField txtArea     = new JTextField(apt != null ? String.valueOf(apt.getArea()) : "");

        JComboBox<String> cbLocation = new JComboBox<>(PROVINCES_FORM);
        if (apt != null && apt.getLocation() != null) cbLocation.setSelectedItem(apt.getLocation());

        JTextField txtDesc     = new JTextField(apt != null && apt.getDescription() != null ? apt.getDescription() : "");

        JPanel pnlInfo = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlInfo.add(new JLabel("Listing Code:")); pnlInfo.add(txtCode);
        pnlInfo.add(new JLabel("Address:"));      pnlInfo.add(txtAddress);
        pnlInfo.add(new JLabel("Location:"));     pnlInfo.add(cbLocation);
        pnlInfo.add(new JLabel("Price ($):"));    pnlInfo.add(txtPrice);
        pnlInfo.add(new JLabel("Bedrooms:"));     pnlInfo.add(txtBedrooms);
        pnlInfo.add(new JLabel("Area (m²):"));    pnlInfo.add(txtArea);
        pnlInfo.add(new JLabel("Description:"));  pnlInfo.add(txtDesc);

        // FR-1.2: Amenity checkboxes — styled as a scrollable box
        List<String> existingAmenities = (apt != null)
            ? service.getAmenitiesForApartment(apt.getId())
            : new ArrayList<>();
        JCheckBox[] checkboxes = new JCheckBox[AMENITIES.length];
        JPanel pnlAmenities = new JPanel(new GridLayout(0, 2, 5, 5));
        pnlAmenities.setBackground(Color.WHITE);
        for (int i = 0; i < AMENITIES.length; i++) {
            checkboxes[i] = new JCheckBox(AMENITIES[i]);
            checkboxes[i].setSelected(existingAmenities.contains(AMENITIES[i]));
            checkboxes[i].setBackground(Color.WHITE);
            pnlAmenities.add(checkboxes[i]);
        }
        
        JScrollPane scrollAmenities = new JScrollPane(pnlAmenities);
        scrollAmenities.setPreferredSize(new Dimension(300, 100));
        scrollAmenities.setBorder(BorderFactory.createTitledBorder("Amenities"));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.add(pnlInfo,         BorderLayout.CENTER);
        mainPanel.add(scrollAmenities, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, mainPanel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Apartment result = new Apartment(
                    apt != null ? apt.getId() : 0,
                    txtCode.getText().trim(),
                    txtAddress.getText().trim(),
                    cbLocation.getSelectedItem().toString(),
                    Double.parseDouble(txtPrice.getText().trim()),
                    Integer.parseInt(txtBedrooms.getText().trim()),
                    Integer.parseInt(txtArea.getText().trim()),
                    ApartmentType.STANDARD, // Fallback Type for DB constraint
                    apt != null ? apt.getCreatedBy() : currentUser.getId(),
                    txtDesc.getText().trim()
                );
                List<String> selectedAmenities = new ArrayList<>();
                for (JCheckBox cb : checkboxes) {
                    if (cb.isSelected()) selectedAmenities.add(cb.getText());
                }
                return new Object[]{result, selectedAmenities};
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input format! Please check all fields.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    /** Write an audit log entry to the database. */
    private void writeAuditLog(int apartmentId, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, apartment_id, action, details) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getId());
            stmt.setInt(2, apartmentId);
            stmt.setString(3, action);
            stmt.setString(4, details);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Audit log write failed: " + e.getMessage());
        }
    }
}