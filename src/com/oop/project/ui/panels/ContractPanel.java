package com.oop.project.ui.panels;

import com.oop.project.model.Customer;
import com.oop.project.model.LeaseContract;
import com.oop.project.model.User;
import com.oop.project.repository.ContractRepository;
import com.oop.project.repository.CustomerRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ContractPanel extends JPanel {
    private final User currentUser;
    private final CustomerRepository customerRepo = new CustomerRepository();
    private final ContractRepository contractRepo = new ContractRepository();

    private JTable contractTable;
    private DefaultTableModel tableModel;

    public ContractPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadContractData();
    }

    private void initComponents() {
        // --- HEADER TIEU ĐỀ ---
        JLabel lblHeader = new JLabel("Lease Contracts Management");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(new Color(44, 62, 80));
        add(lblHeader, BorderLayout.NORTH);

        // --- CENTER: BẢNG DỮ LIỆU ---
        tableModel = new DefaultTableModel(new String[]{"Contract ID", "Customer ID", "Apartment ID", "Start Date", "End Date", "Revenue"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        contractTable = new JTable(tableModel);
        setupTableStyle(contractTable);
        
        JScrollPane scrollPane = new JScrollPane(contractTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: THANH NÚT BẤM (ACTION BAR) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionPanel.setOpaque(false);

        JButton btnAddCustomer = createStyledButton("Add Customer", new Color(41, 128, 185));
        btnAddCustomer.addActionListener(e -> showAddCustomerDialog());

        JButton btnCreateContract = createStyledButton("Create New Contract", new Color(39, 174, 96));
        btnCreateContract.addActionListener(e -> showCreateContractDialog());

        actionPanel.add(btnAddCustomer);
        actionPanel.add(btnCreateContract);
        add(actionPanel, BorderLayout.SOUTH);
    }

    // --- LOGIC: THÊM KHÁCH HÀNG ---
    private void showAddCustomerDialog() {
        JTextField txtName = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();

        Object[] message = {
            "Họ và tên:", txtName,
            "Số điện thoại:", txtPhone,
            "Email:", txtEmail
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Customer", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Customer newCustomer = new Customer(0, txtName.getText(), txtPhone.getText(), txtEmail.getText());
                customerRepo.save(newCustomer);
                JOptionPane.showMessageDialog(this, "Successfully added customer!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving customer: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- LOGIC: TẠO HỢP ĐỒNG ---
    private void showCreateContractDialog() {
        JTextField txtAptId = new JTextField();
        JTextField txtCusId = new JTextField();
        JTextField txtStartDate = new JTextField(LocalDate.now().toString());
        JTextField txtEndDate = new JTextField(LocalDate.now().plusYears(1).toString());
        JTextField txtRent = new JTextField();

        Object[] message = {
            "Available Apartments:", txtAptId,
            "Customer ID:", txtCusId,
            "Start Date (YYYY-MM-DD):", txtStartDate,
            "End Date (YYYY-MM-DD):", txtEndDate,
            "Monthly Rent ($):", txtRent
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New Contract", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int aptId = Integer.parseInt(txtAptId.getText().trim());
                int cusId = Integer.parseInt(txtCusId.getText().trim());
                LocalDate start = LocalDate.parse(txtStartDate.getText().trim());
                LocalDate end = LocalDate.parse(txtEndDate.getText().trim());
                double rent = Double.parseDouble(txtRent.getText().trim());

                LeaseContract contract = new LeaseContract(0, aptId, cusId, start, end, rent);
                contractRepo.save(contract);
                
                // Ở đây bạn có thể gọi thêm logic để cập nhật trạng thái Căn hộ thành RENTED
                
                loadContractData(); // Cập nhật lại bảng
                JOptionPane.showMessageDialog(this, "Successfully created contract! Revenue has been recorded.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input data. Please check the dates or amount.", "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving contract: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadContractData() {
        try {
            List<LeaseContract> list = contractRepo.findAll();
            tableModel.setRowCount(0);
            for (LeaseContract c : list) {
                tableModel.addRow(new Object[]{
                        "HD-" + c.getContractId(),
                        "KH-" + c.getCustomerId(),
                        "CH-" + c.getApartmentId(),
                        c.getStartDate(),
                        c.getEndDate(),
                        String.format("$%,.2f", c.getTotalValue())
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading contracts: " + e.getMessage());
        }
    }

    // --- HÀM UI ---
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40));
        return btn;
    }

    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(236, 240, 241));
        header.setPreferredSize(new Dimension(100, 40));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }
}