package com.oop.project.ui.components;

import com.oop.project.model.Apartment;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class ApartmentTable extends JTable {
    private DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;

    public ApartmentTable() {
        // Cấu hình Model với các cột gốc của bạn
        model = new DefaultTableModel(new String[]{"ID", "Mã", "Địa chỉ", "Giá thuê", "Diện tích"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Khóa không cho người dùng click đúp để sửa trực tiếp trên bảng
            }
        };
        setModel(model);
        sorter = new TableRowSorter<>(model);
        setRowSorter(sorter);

        // --- BẮT ĐẦU "ĐỘ" GIAO DIỆN BẢNG ---
        setupTableStyle();
        setupColumnWidths();
        setupCellRenderers();
    }

    private void setupTableStyle() {
        // 1. Chỉnh font chữ và chiều cao hàng cho thoáng
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setRowHeight(35); // Tăng chiều cao mỗi hàng
        setIntercellSpacing(new Dimension(0, 0)); // Bỏ khoảng cách mặc định giữa các ô
        setShowGrid(false); // Tắt lưới toàn bộ
        setShowHorizontalLines(true); // Chỉ bật đường kẻ ngang
        setGridColor(new Color(230, 230, 230)); // Màu đường kẻ ngang mờ nhẹ

        // 2. Chỉnh màu khi người dùng chọn (Click) vào 1 hàng
        setSelectionBackground(new Color(41, 128, 185)); // Màu xanh dương
        setSelectionForeground(Color.WHITE);

        // 3. Chỉnh Header (Tiêu đề cột)
        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(236, 240, 241)); // Nền xám nhạt
        header.setForeground(new Color(44, 62, 80)); // Chữ xanh đen
        header.setPreferredSize(new Dimension(100, 40)); // Tăng chiều cao header
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200))); // Kẻ viền dưới header
    }

    private void setupColumnWidths() {
        // Chỉnh độ rộng tương đối cho các cột để nhìn cân đối hơn
        getColumnModel().getColumn(0).setPreferredWidth(50);  // ID nhỏ thôi
        getColumnModel().getColumn(0).setMaxWidth(80);
        getColumnModel().getColumn(1).setPreferredWidth(100); // Mã
        getColumnModel().getColumn(2).setPreferredWidth(300); // Địa chỉ (Dài nhất)
        getColumnModel().getColumn(3).setPreferredWidth(120); // Giá
        getColumnModel().getColumn(4).setPreferredWidth(100); // Diện tích
    }

    private void setupCellRenderers() {
        // Căn giữa cho ID và Mã
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Căn giữa và thêm đơn vị cho Diện tích
        DefaultTableCellRenderer areaRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value + " sqft");
            }
        };
        areaRenderer.setHorizontalAlignment(JLabel.CENTER);
        getColumnModel().getColumn(4).setCellRenderer(areaRenderer);

        // Căn phải và format tiền tệ ($) cho cột Giá
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Double) {
                    setText(String.format("$%,.2f", (Double) value));
                } else {
                    super.setValue(value);
                }
            }
        };
        priceRenderer.setHorizontalAlignment(JLabel.RIGHT);
        getColumnModel().getColumn(3).setCellRenderer(priceRenderer);
    }

    // --- OVERRIDE HÀM RENDER ĐỂ TẠO MÀU XEN KẼ (ZEBRA STRIPES) ---
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        // Nếu hàng không được chọn, tô màu xen kẽ
        if (!isRowSelected(row)) {
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            c.setForeground(new Color(44, 62, 80));
        }
        return c;
    }

    // --- CÁC HÀM XỬ LÝ DỮ LIỆU GỐC CỦA BẠN ---
    public void setApartments(List<Apartment> apartments) {
        model.setRowCount(0);
        for (Apartment apt : apartments) {
            model.addRow(new Object[]{
                apt.getId(), 
                apt.getListingCode(), 
                apt.getAddress(), 
                apt.getPrice(), 
                apt.getArea()
            });
        }
    }

    public void setData(List<Apartment> apartments) { 
        setApartments(apartments); 
    }

    public int getSelectedApartmentId() {
        int row = getSelectedRow();
        if (row != -1) {
            // Phải convert index từ View sang Model do có dùng TableRowSorter
            int modelRow = convertRowIndexToModel(row);
            return (int) model.getValueAt(modelRow, 0);
        }
        return -1;
    }
}