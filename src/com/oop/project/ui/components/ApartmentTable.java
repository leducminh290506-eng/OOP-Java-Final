package com.oop.project.ui.components;

import com.oop.project.model.Apartment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ApartmentTable extends JTable {
    private DefaultTableModel tableModel;

    public ApartmentTable() {
        String[] columns = {"ID", "Title", "Address", "Price", "Area", "Type", "Status", "Favorite"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };
        setModel(tableModel);
    }

    public void setData(List<Apartment> apartments) {
        tableModel.setRowCount(0);
        for (Apartment apt : apartments) {
            tableModel.addRow(new Object[]{
                apt.getId(),
                apt.getTitle(),
                apt.getAddress(),
                apt.getPrice(),
                apt.getArea(),
                apt.getType().name(),
                apt.getStatus(),
                apt.isFavorite() ? "Yes" : "No"
            });
        }
    }

    public int getSelectedApartmentId() {
        int row = getSelectedRow();
        if (row >= 0) {
            return (int) tableModel.getValueAt(row, 0);
        }
        return -1;
    }
}