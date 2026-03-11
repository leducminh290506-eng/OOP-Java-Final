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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ListingPanel - Quản lý danh sách căn hộ.
 * - Tích hợp phân quyền Admin cho chức năng xóa (FR-0.4).
 * - Hỗ trợ đánh dấu yêu thích và ghi chú nội bộ (FR-3).
 */
public class ListingPanel extends JPanel {

    private final ApartmentService apartmentService;
    private final User currentUser;

    private final ApartmentTable table;
    private final JTextField txtMinPrice;
    private final JTextField txtMaxPrice;
    private final JTextField txtSearch;
    private final JComboBox<String> cboCategory;

    private final NoteRepository noteRepository = new NoteRepository();

    public ListingPanel(ApartmentService service, User user) {
        this.apartmentService = service;
        this.currentUser = user;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- HÀNG 0: BỘ LỌC GIÁ NHANH ---
        JLabel lblMin = new JLabel("Giá từ:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        add(lblMin, gbc);

        txtMinPrice = new JTextField(8);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.2;
        add(txtMinPrice, gbc);

        JLabel lblMax = new JLabel("đến:");
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        add(lblMax, gbc);

        txtMaxPrice = new JTextField(8);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.2;
        add(txtMaxPrice, gbc);

        JButton btnFilter = new JButton("Lọc");
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0;
        add(btnFilter, gbc);

        JButton btnReset = new JButton("Tất cả");
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0;
        add(btnReset, gbc);

        // --- HÀNG 1: SEARCH + FILTER CATEGORY (FR-1.4, FR-5.2, FR-5.3) ---
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(lblSearch, gbc);

        txtSearch = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.4;
        add(txtSearch, gbc);

        JButton btnSearch = new JButton("Search");
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        add(btnSearch, gbc);

        JLabel lblCat = new JLabel("Category:");
        gbc.gridx = 4; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        add(lblCat, gbc);

        cboCategory = new JComboBox<>(new String[]{"All", "Luxury", "Standard", "Budget"});
        gbc.gridx = 5; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2;
        add(cboCategory, gbc);

        // --- HÀNG 2: CÁC NÚT CRUD & THAO TÁC ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnAdd = new JButton("Thêm mới");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnFavorite = new JButton("Yêu thích/Bỏ yêu thích");
        JButton btnNote = new JButton("Ghi chú");
        JButton btnManageNotes = new JButton("Sửa/Xóa ghi chú");
        JButton btnDetail = new JButton("Chi tiết");

        // Định dạng nút Xóa
        btnDelete.setForeground(Color.RED);

        // PHÂN QUYỀN (FR-0.4): Chỉ ADMIN mới thấy và dùng được nút Xóa
        if (currentUser.getRole() != Role.ADMIN) {
            btnDelete.setEnabled(false);
            btnDelete.setVisible(false); // Ẩn hoàn toàn nút xóa đối với Agent
        }

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnFavorite);
        actionPanel.add(btnNote);
        actionPanel.add(btnManageNotes);
        actionPanel.add(btnDetail);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        add(actionPanel, gbc);

        // --- HÀNG 3: BẢNG DỮ LIỆU ---
        table = new ApartmentTable();
        JScrollPane scrollPane = new JScrollPane(table);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);

        // --- SỰ KIỆN NÚT BẤM ---
        btnFilter.addActionListener(e -> handleFilter());
        btnReset.addActionListener(e -> loadAllData());
        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        btnFavorite.addActionListener(e -> handleToggleFavorite());
        btnNote.addActionListener(e -> handleAddNote());
        btnManageNotes.addActionListener(e -> handleManageNotes());
        btnDetail.addActionListener(e -> handleShowDetail());
        btnSearch.addActionListener(e -> applySearchAndCategory());
        cboCategory.addActionListener(e -> applySearchAndCategory());

        // Tải dữ liệu ban đầu
        loadAllData();
    }

    private void handleFilter() {
        double min = 0;
        double max = Double.MAX_VALUE;

        try {
            String minText = txtMinPrice.getText().trim();
            if (!minText.isEmpty()) min = Double.parseDouble(minText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá tối thiểu không hợp lệ");
            return;
        }

        try {
            String maxText = txtMaxPrice.getText().trim();
            if (!maxText.isEmpty()) max = Double.parseDouble(maxText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá tối đa không hợp lệ");
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
            JOptionPane.showMessageDialog(this, "Thêm căn hộ thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm căn hộ: " + ex.getMessage());
        }
    }

    private void handleEdit() {
        int id = table.getSelectedApartmentId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ cần sửa!");
            return;
        }

        try {
            Apartment current = apartmentService.getApartmentById(id);
            ApartmentFormResult form = showApartmentForm(current);
            if (form == null) return;

            apartmentService.updateApartmentWithAmenities(form.apartment, form.amenities, currentUser.getId());
            loadAllData();
            JOptionPane.showMessageDialog(this, "Cập nhật căn hộ thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật căn hộ: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        int id = table.getSelectedApartmentId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Xóa căn hộ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            apartmentService.deleteApartment(id, currentUser.getId());
            loadAllData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage());
        }
    }

    private void handleToggleFavorite() {
        int id = table.getSelectedApartmentId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để thêm/bỏ yêu thích!");
            return;
        }

        try {
            apartmentService.toggleFavorite(currentUser.getId(), id);
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái yêu thích.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật yêu thích: " + ex.getMessage());
        }
    }

    /**
     * Thêm ghi chú nội bộ cho căn hộ (FR-3.2, FR-3.4).
     */
    private void handleAddNote() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để ghi chú!");
            return;
        }

        // Load các note cũ (nếu có) để agent dễ theo dõi lịch sử
        List<Note> existingNotes = noteRepository.findByApartmentId(apartmentId);
        StringBuilder existing = new StringBuilder();
        if (!existingNotes.isEmpty()) {
            existing.append("Các ghi chú hiện có:\n");
            for (Note n : existingNotes) {
                existing.append("- [User ")
                        .append(n.getUserId())
                        .append("] ")
                        .append(n.getNoteText())
                        .append("\n");
            }
            existing.append("\n----- Nhập ghi chú mới bên dưới -----\n");
        }

        JTextArea txtArea = new JTextArea(8, 30);
        txtArea.setWrapStyleWord(true);
        txtArea.setLineWrap(true);
        if (existing.length() > 0) {
            txtArea.setText(existing.toString());
            txtArea.setCaretPosition(txtArea.getText().length());
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(txtArea),
                "Ghi chú cho căn hộ ID = " + apartmentId,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String content = txtArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung ghi chú không được để trống!");
            return;
        }

        try {
            Note note = new Note(0, apartmentId, currentUser.getId(), content);
            noteRepository.save(note);
            JOptionPane.showMessageDialog(this, "Đã lưu ghi chú.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu ghi chú: " + ex.getMessage());
        }
    }

    private void loadAllData() {
        try {
            applySearchAndCategory();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
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
        JTextField txtCode = new JTextField(existing != null ? existing.getListingCode() : "");
        JTextField txtAddress = new JTextField(existing != null ? existing.getAddress() : "");
        JTextField txtLocation = new JTextField(existing != null ? existing.getLocation() : "");
        JTextField txtPrice = new JTextField(existing != null ? String.valueOf(existing.getPrice()) : "");
        JTextField txtBedrooms = new JTextField(existing != null ? String.valueOf(existing.getBedrooms()) : "");
        JTextField txtArea = new JTextField(existing != null ? String.valueOf(existing.getArea()) : "");
        JComboBox<ApartmentType> cboType = new JComboBox<>(ApartmentType.values());
        if (existing != null) cboType.setSelectedItem(existing.getType());

        // Amenities selector (FR-1.2)
        List<String> allAmenities = apartmentService.getAllAmenityNames();
        JList<String> listAmenities = new JList<>(allAmenities.toArray(new String[0]));
        listAmenities.setVisibleRowCount(6);
        listAmenities.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (existing != null) {
            List<String> selected = apartmentService.getAmenitiesForApartment(existing.getId());
            int[] indices = selected.stream()
                    .mapToInt(allAmenities::indexOf)
                    .filter(i -> i >= 0)
                    .toArray();
            listAmenities.setSelectedIndices(indices);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Mã:")); form.add(txtCode);
        form.add(new JLabel("Địa chỉ:")); form.add(txtAddress);
        form.add(new JLabel("Vị trí:")); form.add(txtLocation);
        form.add(new JLabel("Giá:")); form.add(txtPrice);
        form.add(new JLabel("Số phòng ngủ:")); form.add(txtBedrooms);
        form.add(new JLabel("Diện tích:")); form.add(txtArea);
        form.add(new JLabel("Loại căn hộ:")); form.add(cboType);
        form.add(new JLabel("Amenities:")); form.add(new JScrollPane(listAmenities));

        String title = (existing == null) ? "Thêm căn hộ mới" : "Sửa căn hộ";
        int result = JOptionPane.showConfirmDialog(
                this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            int bedrooms = Integer.parseInt(txtBedrooms.getText().trim());
            int area = Integer.parseInt(txtArea.getText().trim());
            ApartmentType type = (ApartmentType) cboType.getSelectedItem();

            int id = (existing != null) ? existing.getId() : 0;
            int createdBy = (existing != null) ? existing.getCreatedBy() : currentUser.getId();

            Apartment apt = new Apartment(
                    id,
                    txtCode.getText().trim(),
                    txtAddress.getText().trim(),
                    txtLocation.getText().trim(),
                    price,
                    bedrooms,
                    area,
                    type,
                    createdBy
            );
            List<String> amenities = listAmenities.getSelectedValuesList();
            return new ApartmentFormResult(apt, amenities);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá, số phòng ngủ và diện tích phải là số.");
            return null;
        }
    }

    private void handleManageNotes() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để quản lý ghi chú!");
            return;
        }

        NotesDialog dialog = new NotesDialog(
                SwingUtilities.getWindowAncestor(this),
                noteRepository,
                apartmentId,
                currentUser.getId()
        );
        dialog.setVisible(true);
    }

    private void handleShowDetail() {
        int apartmentId = table.getSelectedApartmentId();
        if (apartmentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn căn hộ để xem chi tiết!");
            return;
        }

        try {
            Apartment apt = apartmentService.getApartmentById(apartmentId);
            List<String> amenities = apartmentService.getAmenitiesForApartment(apartmentId);
            ApartmentDetailDialog dialog = new ApartmentDetailDialog(
                    SwingUtilities.getWindowAncestor(this),
                    apt,
                    amenities
            );
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết: " + ex.getMessage());
        }
    }
}
