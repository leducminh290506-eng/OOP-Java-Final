package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * ApartmentDetailDialog - Summary chi tiết (FR-4.2).
 */
public class ApartmentDetailDialog extends JDialog {
    public ApartmentDetailDialog(Window owner, Apartment apartment, List<String> amenities) {
        super(owner, "Chi tiết căn hộ " + apartment.getListingCode(), ModalityType.APPLICATION_MODAL);
        setSize(520, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new GridLayout(0, 1));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        header.add(new JLabel("Mã: " + apartment.getListingCode()));
        header.add(new JLabel("Địa chỉ: " + apartment.getAddress()));
        header.add(new JLabel("Vị trí: " + apartment.getLocation()));
        header.add(new JLabel("Loại: " + apartment.getType()));
        header.add(new JLabel("Category (FR-4.3): " + apartment.getCategory()));
        add(header, BorderLayout.NORTH);

        double price = apartment.getPrice();
        int area = apartment.getArea();
        double pricePerSqft = (area > 0) ? (price / area) : 0.0;

        JTextArea body = new JTextArea();
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Consolas", Font.PLAIN, 13));

        StringBuilder sb = new StringBuilder();
        sb.append("Price breakdown\n");
        sb.append("- Monthly rent: $").append(String.format("%.2f", price)).append("\n");
        sb.append("- Bedrooms: ").append(apartment.getBedrooms()).append("\n");
        sb.append("- Area: ").append(area).append(" sqft\n");
        sb.append("- Price / sqft: $").append(String.format("%.4f", pricePerSqft)).append("\n\n");

        sb.append("Amenities\n");
        if (amenities == null || amenities.isEmpty()) {
            sb.append("- (none)\n");
        } else {
            for (String a : amenities) {
                sb.append("- ").append(a).append("\n");
            }
        }
        body.setText(sb.toString());
        body.setCaretPosition(0);

        add(new JScrollPane(body), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
    }
}

