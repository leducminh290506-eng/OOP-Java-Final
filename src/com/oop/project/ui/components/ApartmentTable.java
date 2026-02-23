package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ApartmentTable extends JTable {
    private DefaultTableModel model;

    public ApartmentTable() {
        model = new DefaultTableModel(new String[]{"ID", "Mã", "Địa chỉ", "Giá", "Diện tích"}, 0);
        setModel(model);
    }

    // Xóa lỗi "The method setApartments is undefined" [image_e4395d.png]
    public void setApartments(List<Apartment> apartments) {
        model.setRowCount(0);
        for (Apartment apt : apartments) {
            model.addRow(new Object[]{apt.getId(), apt.getListingCode(), apt.getAddress(), apt.getPrice(), apt.getArea()});
        }
    }

    public void setData(List<Apartment> apartments) { setApartments(apartments); }

    public int getSelectedApartmentId() {
        int row = getSelectedRow();
        return (row != -1) ? (int) model.getValueAt(row, 0) : -1;
    }
}