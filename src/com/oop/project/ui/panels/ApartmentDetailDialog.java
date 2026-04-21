package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;
import com.oop.project.model.Note;
import com.oop.project.repository.NoteRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * ApartmentDetailDialog - full detail view including amenities & notes.
 */
public class ApartmentDetailDialog extends JDialog {

    public ApartmentDetailDialog(Window owner, Apartment apartment, List<String> amenities) {
        this(owner, apartment, amenities, null, -1);
    }

    public ApartmentDetailDialog(Window owner, Apartment apartment, List<String> amenities,
                                  NoteRepository noteRepo, int aptId) {
        super(owner, "Apartment Detail - " + apartment.getListingCode(), ModalityType.APPLICATION_MODAL);
        setSize(500, 650);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ── HEADER ────────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel lblTitle = new JLabel("APARTMENT " + apartment.getListingCode());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Category only (no type to avoid redundancy)
        JLabel lblCategory = new JLabel(apartment.getCategory() != null ? apartment.getCategory().toUpperCase() : "");
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCategory.setForeground(new Color(236, 240, 241));
        lblCategory.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description if set
            headerPanel.add(lblTitle);
            headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            headerPanel.add(lblCategory);

        // ── BODY ──────────────────────────────────────────────────────────────
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Basic Info
        bodyPanel.add(createSectionTitle("Basic Information"));
        bodyPanel.add(createInfoRow("Address:", apartment.getAddress()));
        bodyPanel.add(createInfoRow("Location:", apartment.getLocation()));
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Price & Space
        double price = apartment.getPrice();
        int area = apartment.getArea();
        double pricePerSqft = (area > 0) ? (price / area) : 0.0;

        bodyPanel.add(createSectionTitle("Price & Space Details"));
        bodyPanel.add(createInfoRow("Rent Price (Monthly):", String.format("$%,.2f", price)));
        bodyPanel.add(createInfoRow("Bedrooms:", String.valueOf(apartment.getBedrooms())));
        bodyPanel.add(createInfoRow("Area:", area + " m²"));
        bodyPanel.add(createInfoRow("Price / m²:", String.format("$%,.2f", pricePerSqft)));
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Amenities
        bodyPanel.add(createSectionTitle("Amenities"));
        JPanel amenitiesPanel = new JPanel(new GridLayout(0, 2, 10, 6));
        amenitiesPanel.setBackground(Color.WHITE);
        amenitiesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (amenities == null || amenities.isEmpty()) {
            amenitiesPanel.add(new JLabel("No amenities listed"));
        } else {
            for (String a : amenities) {
                JLabel lbl = new JLabel("- " + a);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lbl.setForeground(new Color(52, 73, 94));
                amenitiesPanel.add(lbl);
            }
        }
        bodyPanel.add(amenitiesPanel);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Description
        String desc = apartment.getDescription();
        if (desc != null && !desc.isBlank()) {
            bodyPanel.add(createSectionTitle("Description"));
            JTextArea taDesc = new JTextArea(desc);
            taDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            taDesc.setForeground(new Color(44, 62, 80));
            taDesc.setBackground(Color.WHITE);
            taDesc.setEditable(false);
            taDesc.setLineWrap(true);
            taDesc.setWrapStyleWord(true);
            taDesc.setBorder(null);
            taDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(taDesc);
            bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        // Notes section (Agent Notes)
        List<Note> notes = null;
        if (noteRepo != null && aptId > 0) {
            try {
                notes = noteRepo.findByApartmentId(aptId);
            } catch (Exception e) {
                // silently ignore
            }
        }
        if (notes != null && !notes.isEmpty()) {
            bodyPanel.add(createSectionTitle("Agent Notes"));
            for (Note note : notes) {
                JTextArea ta = new JTextArea(note.getNoteText());
                ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                ta.setForeground(new Color(44, 62, 80));
                ta.setBackground(new Color(248, 250, 252));
                ta.setEditable(false);
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                ta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 218, 226)),
                    new EmptyBorder(6, 8, 6, 8)
                ));
                ta.setAlignmentX(Component.LEFT_ALIGNMENT);
                ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
                bodyPanel.add(ta);
                bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // ── FOOTER ────────────────────────────────────────────────────────────
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(new Color(231, 76, 60));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setOpaque(true);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnClose.setBackground(new Color(192, 57, 43)); }
            public void mouseExited(java.awt.event.MouseEvent evt)  { btnClose.setBackground(new Color(231, 76, 60)); }
        });
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel createSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(41, 128, 185));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    private JPanel createInfoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(2, 0, 8, 0)
        ));

        JLabel lblKey = new JLabel(key);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKey.setForeground(new Color(127, 140, 141));

        JLabel lblValue = new JLabel(value != null ? value : "");
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblValue.setForeground(new Color(44, 62, 80));

        row.add(lblKey,   BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        return row;
    }
}