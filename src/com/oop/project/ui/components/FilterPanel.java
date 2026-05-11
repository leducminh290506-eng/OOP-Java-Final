package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import com.oop.project.service.ApartmentService;
import com.oop.project.service.ExportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * FilterPanel — Simplified: Search box + Price range only.
 * All filters apply live (no Apply button). Export CSV available.
 */
public class FilterPanel extends JPanel {

    private final ApartmentService service;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Filter widgets — simplified: only search + price range
    private JTextField txtSearch, txtPriceFrom, txtPriceTo;

    public FilterPanel(ApartmentService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(18, 18, 18, 18));
        setBackground(Color.WHITE);
        initComponents();
        // Load all data on init so user sees results immediately
        executeFilter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void initComponents() {

        // ── NORTH: Compact criteria bar ─────────────────────────────────────
        JPanel pnlCriteria = buildCriteriaPanel();

        // ── CENTER: Sortable result table ───────────────────────────────────
        String[] columns = {"ID", "Code", "Address", "Location", "Price ($)", "Bedrooms", "Area (m²)", "Category", "Status"};
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

        // ── SOUTH: Action buttons ────────────────────────────────────────────
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
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

        JButton btnExport = styledBtn("Export CSV", new Color(39, 174, 96));
        btnExport.addActionListener(e -> handleExportCSV());

        pnlFooter.add(btnExport);
        pnlFooter.add(btnDetail);

        add(pnlCriteria, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(pnlFooter,   BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Build the minimal filter criteria area (single row)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCriteriaPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Enter keyword, listing code, address...");

        txtPriceFrom = new JTextField(6);
        txtPriceTo   = new JTextField(6);

        JButton btnReset = new JButton("Clear Filters");
        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReset.addActionListener(e -> resetFilters());

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Price $:"));
        filterPanel.add(txtPriceFrom);
        filterPanel.add(new JLabel("–"));
        filterPanel.add(txtPriceTo);
        filterPanel.add(btnReset);

        // ── Live update listeners (auto-filter on every keystroke) ────────────
        DocumentListener live = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { executeFilter(); }
            public void removeUpdate(DocumentEvent e)  { executeFilter(); }
            public void changedUpdate(DocumentEvent e) {}
        };
        txtSearch   .getDocument().addDocumentListener(live);
        txtPriceFrom.getDocument().addDocumentListener(live);
        txtPriceTo  .getDocument().addDocumentListener(live);

        return filterPanel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Filter logic — searches all text fields: code, address, location,
    //  description, amenities, category
    // ─────────────────────────────────────────────────────────────────────────
    private String normalize(String s) {
        if (s == null) return "";
        String r = Normalizer.normalize(s, Normalizer.Form.NFD);
        r = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(r).replaceAll("").toLowerCase();
        return r.replace("tp. ", "").replace("tp.", "").replace("tp ", "")
                .replace("hcm", "ho chi minh").replace("hn", "ha noi").replace("dn", "da nang").trim();
    }

    private void executeFilter() {
        String keyword = normalize(txtSearch.getText());

        Double minPrice = null, maxPrice = null;
        try {
            if (!txtPriceFrom.getText().trim().isEmpty()) minPrice = Double.parseDouble(txtPriceFrom.getText().trim());
            if (!txtPriceTo.getText().trim().isEmpty())   maxPrice = Double.parseDouble(txtPriceTo.getText().trim());
        } catch (NumberFormatException ex) { return; }

        List<Apartment> all = service.findAll();
        tableModel.setRowCount(0);

        for (Apartment a : all) {
            // Keyword: searches across code, address, location, category, description, amenities
            boolean mKey = true;
            if (!keyword.isEmpty()) {
                List<String> aptAmenities = service.getAmenitiesForApartment(a.getId());
                String amenText = normalize(String.join(" ", aptAmenities));
                String dbDesc = normalize(a.getDescription());

                mKey = normalize(a.getListingCode()).contains(keyword)
                    || normalize(a.getAddress()).contains(keyword)
                    || normalize(a.getLocation()).contains(keyword)
                    || normalize(a.getCategory()).contains(keyword)
                    || amenText.contains(keyword)
                    || dbDesc.contains(keyword);
            }

            // Price range
            boolean mPrice = true;
            if (minPrice != null && a.getPrice() < minPrice) mPrice = false;
            if (maxPrice != null && a.getPrice() > maxPrice) mPrice = false;

            if (mKey && mPrice) {
                String rentalStatus = service.getRentalStatus(a.getId());
                tableModel.addRow(new Object[]{
                    a.getId(), a.getListingCode(), a.getAddress(), a.getLocation(),
                    a.getPrice(), a.getBedrooms(), a.getArea(), a.getCategory(), rentalStatus
                });
            }
        }
    }

    private void resetFilters() {
        txtSearch.setText("");
        txtPriceFrom.setText("");
        txtPriceTo.setText("");
        executeFilter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Export CSV
    // ─────────────────────────────────────────────────────────────────────────
    private void handleExportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export filtered listings to CSV");
        chooser.setSelectedFile(new File("filtered_export.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

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