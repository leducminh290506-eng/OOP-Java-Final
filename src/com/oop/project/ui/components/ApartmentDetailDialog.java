package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ApartmentDetailDialog extends JDialog {
    
    public ApartmentDetailDialog(Frame parent, Apartment apt) {
        super(parent, "Apartment Detail", true);
        setSize(400, 400); // Tăng chút chiều cao để chứa thêm dòng
        setLocationRelativeTo(parent); 
        
        JPanel p = new JPanel(new GridLayout(0, 1, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Đã thêm Address và Location, đổi sqft thành m²
        p.add(new JLabel("<html><b>Code:</b> " + apt.getListingCode() + "</html>"));
        p.add(new JLabel("<html><b>Address:</b> " + apt.getAddress() + "</html>"));
        p.add(new JLabel("<html><b>Location:</b> " + apt.getLocation() + "</html>"));
        p.add(new JLabel("<html><b>Price:</b> $" + apt.getPrice() + "</html>"));
        p.add(new JLabel("<html><b>Bedrooms:</b> " + apt.getBedrooms() + "</html>"));
        p.add(new JLabel("<html><b>Area:</b> " + apt.getArea() + " m²</html>"));
        p.add(new JLabel("<html><b>Type/Category:</b> " + apt.getType() + "</html>"));

        JButton btn = new JButton("Close");
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> dispose());
        
        add(p, BorderLayout.CENTER);
        add(btn, BorderLayout.SOUTH);
    }
}