package com.oop.project.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class FilterPanel extends JPanel {
    private JTextField txtMinPrice;
    private JTextField txtMaxPrice;
    private JButton btnFilter;

    public FilterPanel(ActionListener filterAction) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        add(new JLabel("Min Price:"));
        txtMinPrice = new JTextField(10);
        add(txtMinPrice);
        
        add(new JLabel("Max Price:"));
        txtMaxPrice = new JTextField(10);
        add(txtMaxPrice);
        
        btnFilter = new JButton("Filter");
        btnFilter.addActionListener(filterAction);
        add(btnFilter);
    }

    public double getMinPrice() {
        try {
            return Double.parseDouble(txtMinPrice.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getMaxPrice() {
        try {
            return Double.parseDouble(txtMaxPrice.getText());
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }
}