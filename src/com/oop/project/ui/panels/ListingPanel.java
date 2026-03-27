package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.model.Note;
import com.oop.project.model.Role;
import com.oop.project.model.User;
import com.oop.project.repository.NoteRepository;
import com.oop.project.service.ApartmentService;
import com.oop.project.ui.components.ApartmentTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ListingPanel extends JPanel {

    private final ApartmentService apartmentService;
    private final User currentUser;

    private ApartmentTable table;
    private JTextField txtMinPrice;
    private JTextField txtMaxPrice;
    private JTextField txtSearch;
    private JComboBox<String> cboCategory;

    private final NoteRepository noteRepository = new NoteRepository();

    public ListingPanel(ApartmentService service, User user) {
        this.apartmentService = service;
        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding xung quanh toàn bộ panel
        setBackground(new Color(245, 247, 250)); // Màu nền xám nhạt hiện đại

        initComponents();
        loadAllData();
    }

    private void initComponents() {
        // --- 1. KHU VỰC TÌM KIẾM & LỌC (TOP PANEL) ---
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        // 1.1 Bộ lọc giá & Phân loại
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        filterPanel.add(new JLabel("🔍 Tìm kiếm:"));
        txtSearch = createStyledTextField(15);
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("🏷️ Danh mục:"));
        cboCategory = new JComboBox<>(new String[]{"All", "Luxury", "Standard", "Budget"});
        cboCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterPanel.add(cboCategory);

        filterPanel.add(new JLabel("💵 Giá từ:"));
        txtMinPrice = createStyledTextField(8);
        filterPanel.add(txtMinPrice);

        filterPanel.add(new JLabel("đến:"));
        txtMaxPrice = createStyledTextField(8);
        filterPanel.add(txtMaxPrice);

        JButton btnFilter = styleButton(new JButton("Lọc Dữ Liệu"), new Color(41, 128, 185), Color.WHITE);
        JButton btnReset = styleButton(new JButton("Làm Mới"), new Color(149, 165, 166), Color.WHITE);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        // 1.2 Thanh công cụ thao tác (Action Bar)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setOpaque(false);

        JButton btnAdd = styleButton(new JButton("➕ Thêm mới"), new Color(39, 174, 96), Color.WHITE);
        JButton btnEdit = styleButton(new JButton("✏️ Sửa"), new Color(243, 156, 18), Color.WHITE);
        JButton btnDelete = styleButton(new JButton("🗑️ Xóa"), new Color(231, 76, 60), Color.WHITE);
        JButton btnDetail = styleButton(new JButton("📄 Chi tiết"), new Color(52, 73, 94), Color.WHITE);
        JButton btnFavorite = styleButton(new JButton("❤️ Yêu thích"), new Color(224, 86, 253), Color.WHITE);
        JButton btnNote = styleButton(new JButton("📝 Ghi chú"), new Color(142, 68, 173), Color.WHITE);
        JButton btnManageNotes = styleButton(new JButton("⚙️ QL Ghi chú"), new Color(142, 68, 173), Color.WHITE);

        // Phân quyền (Chỉ Admin mới được xóa)
        if (currentUser.getRole() != Role.ADMIN) {
            btnDelete.setVisible(false);
        }

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(new JSeparator(SwingConstants.VERTICAL)); // Vách ngăn
        actionPanel.add(btnDetail);
        actionPanel.add(btnFavorite);
        actionPanel.add(btnNote);
        actionPanel.add(btnManageNotes);

        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU (CENTER PANEL) ---
        table = new ApartmentTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. ĐĂNG KÝ SỰ KIỆN ---
        btnFilter.addActionListener(e -> handleFilter());
        btnReset.addActionListener(e -> {
            txtMinPrice.setText("");
            txtMaxPrice.setText("");
            txtSearch.setText("");
            cboCategory.setSelectedIndex(0);
            loadAllData();
        });
        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        btnFavorite.addActionListener(e -> handleToggleFavorite());
        btnNote.addActionListener(e -> handleAddNote());
        btnManageNotes.addActionListener(e -> handleManageNotes());
        btnDetail.addActionListener(e -> handleShowDetail());
        
        // Cập nhật tìm kiếm real-time (tùy chọn) hoặc khi ấn Lọc
        txtSearch.addActionListener(e -> applySearchAndCategory());
        cboCategory.addActionListener(e -> applySearchAndCategory());
    }

    // --- HELPER UI METHODS ---
    private JTextField createStyledTextField(int columns) {
        JTextField txt = new JTextField(columns);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)
        ));
        return txt;
    }

    private JButton styleButton(JButton btn, Color bgColor, Color fgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    // --- CÁC HÀM XỬ LÝ LOGIC (Giữ nguyên logic cũ của bạn) ---
    private void handleFilter() {
        double min = 0;
        double max = Double.MAX_VALUE;

        try {
            String minText = txtMinPrice.getText().trim();
            if (!minText.isEmpty()) min = Double.parseDouble(minText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá tối thiểu không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String maxText = txtMaxPrice.getText().trim();
            if (!maxText.isEmpty()) max = Double.parseDouble(maxText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá tối đa không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            table.setApartments(apartmentService.filterByPrice(min, max));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lọc dữ liệu: " + ex.getMessage());
        }
    }

    private void handleAdd() {
        ApartmentFormResult form = showApartmentForm(null);
        if (form == null) return;
        try {
            apartmentService.createApartmentWithAmenities(form.apartment, form.amenities, currentUser.getId());
            loadAllData();
            JOptionPane.showMessageDialog(this, "Thêm căn hộ thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm căn hộ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEdit() {
        int id = table.getSelectedApartmentId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Apartment current = apartmentService.getApartmentById(id);
            ApartmentFormResult form = showApartmentForm(current);
            if (form == null) return;

            apartmentService.updateApartmentWithAmenities(form.apartment, form.amenities, currentUser.getId());
            loadAllData();
            JOptionPane.showMessageDialog(this, "Cập nhật căn hộ thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int id = table.getSelectedApartmentId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa căn hộ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            apartmentService.deleteApartment(id, currentUser.getId());
            loadAllData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleToggleFavorite() {
        int id = table.getSelectedApartmentId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            apartmentService.toggleFavorite(currentUser.getId(), id);
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái yêu thích.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddNote() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để ghi chú!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Note> existingNotes = noteRepository.findByApartmentId(apartmentId);
        StringBuilder existing = new StringBuilder();
        if (!existingNotes.isEmpty()) {
            existing.append("Các ghi chú hiện có:\n");
            for (Note n : existingNotes) {
                existing.append("- [User ").append(n.getUserId()).append("] ")
                        .append(n.getNoteText()).append("\n");
            }
            existing.append("\n----- Nhập ghi chú mới bên dưới -----\n");
        }

        JTextArea txtArea = new JTextArea(8, 40);
        txtArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtArea.setWrapStyleWord(true);
        txtArea.setLineWrap(true);
        if (existing.length() > 0) {
            txtArea.setText(existing.toString());
            txtArea.setCaretPosition(txtArea.getText().length());
        }

        int result = JOptionPane.showConfirmDialog(
                this, new JScrollPane(txtArea), "Ghi chú cho căn hộ ID: " + apartmentId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String content = txtArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nội dung không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                noteRepository.save(new Note(0, apartmentId, currentUser.getId(), content));
                JOptionPane.showMessageDialog(this, "Đã lưu ghi chú.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleManageNotes() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để quản lý ghi chú!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Giả định NotesDialog đã tồn tại
        // NotesDialog dialog = new NotesDialog(SwingUtilities.getWindowAncestor(this), noteRepository, apartmentId, currentUser.getId());
        // dialog.setVisible(true);
    }

    private void handleShowDetail() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để xem chi tiết!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Apartment apt = apartmentService.getApartmentById(apartmentId);
            List<String> amenities = apartmentService.getAmenitiesForApartment(apartmentId);
            // Giả định ApartmentDetailDialog đã tồn tại
            // ApartmentDetailDialog dialog = new ApartmentDetailDialog(SwingUtilities.getWindowAncestor(this), apt, amenities);
            // dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết: " + ex.getMessage());
        }
    }

    private void loadAllData() {
        applySearchAndCategory();
    }

    private void applySearchAndCategory() {
        String keyword = txtSearch.getText().trim();
        String cat = (String) cboCategory.getSelectedItem();
        if (cat == null) cat = "All";

        List<Apartment> base = keyword.isEmpty()
                ? apartmentService.getAllApartments()
                : apartmentService.searchApartments(keyword);

        if (!"All".equalsIgnoreCase(cat)) {
            List<Apartment> filtered = new ArrayList<>();
            for (Apartment a : base) {
                if (cat.equalsIgnoreCase(a.getCategory())) filtered.add(a);
            }
            base = filtered;
        }
        table.setApartments(base);
    }

    private static class ApartmentFormResult {
        final Apartment apartment;
        final List<String> amenities;
        ApartmentFormResult(Apartment apartment, List<String> amenities) {
            this.apartment = apartment;
            this.amenities = amenities;
        }
    }

    private ApartmentFormResult showApartmentForm(Apartment existing) {
        JTextField txtCode = createStyledTextField(15);
        JTextField txtAddress = createStyledTextField(15);
        JTextField txtLocation = createStyledTextField(15);
        JTextField txtPrice = createStyledTextField(15);
        JTextField txtBedrooms = createStyledTextField(15);
        JTextField txtArea = createStyledTextField(15);
        JComboBox<ApartmentType> cboType = new JComboBox<>(ApartmentType.values());

        if (existing != null) {
            txtCode.setText(existing.getListingCode());
            txtAddress.setText(existing.getAddress());
            txtLocation.setText(existing.getLocation());
            txtPrice.setText(String.valueOf(existing.getPrice()));
            txtBedrooms.setText(String.valueOf(existing.getBedrooms()));
            txtArea.setText(String.valueOf(existing.getArea()));
            cboType.setSelectedItem(existing.getType());
        }

        List<String> allAmenities = apartmentService.getAllAmenityNames();
        JList<String> listAmenities = new JList<>(allAmenities.toArray(new String[0]));
        listAmenities.setVisibleRowCount(6);
        listAmenities.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        if (existing != null) {
            List<String> selected = apartmentService.getAmenitiesForApartment(existing.getId());
            int[] indices = selected.stream().mapToInt(allAmenities::indexOf).filter(i -> i >= 0).toArray();
            listAmenities.setSelectedIndices(indices);
        }

        // Tinh chỉnh form nhập liệu
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Component[] labels = {
            new JLabel("Mã căn hộ:"), new JLabel("Địa chỉ:"), new JLabel("Vị trí:"),
            new JLabel("Giá (USD):"), new JLabel("Số phòng ngủ:"), new JLabel("Diện tích (m²):"),
            new JLabel("Loại căn hộ:"), new JLabel("Tiện ích đi kèm:")
        };
        Component[] fields = {txtCode, txtAddress, txtLocation, txtPrice, txtBedrooms, txtArea, cboType, new JScrollPane(listAmenities)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            form.add(labels[i], gbc);
            gbc.gridx = 1; gbc.gridy = i; gbc.weightx = 0.7;
            form.add(fields[i], gbc);
        }

        String title = (existing == null) ? "Thêm Căn Hộ Mới" : "Chỉnh Sửa Căn Hộ";
        int result = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            int bedrooms = Integer.parseInt(txtBedrooms.getText().trim());
            int area = Integer.parseInt(txtArea.getText().trim());
            ApartmentType type = (ApartmentType) cboType.getSelectedItem();
            int id = (existing != null) ? existing.getId() : 0;
            int createdBy = (existing != null) ? existing.getCreatedBy() : currentUser.getId();

            Apartment apt = new Apartment(id, txtCode.getText().trim(), txtAddress.getText().trim(),
                    txtLocation.getText().trim(), price, bedrooms, area, type, createdBy);
            return new ApartmentFormResult(apt, listAmenities.getSelectedValuesList());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá, số phòng ngủ và diện tích phải là số!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}