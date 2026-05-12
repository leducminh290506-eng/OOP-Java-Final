package com.oop.project.model;

import java.time.LocalDate;

public class LeaseContract {
    private int contractId;
    private int apartmentId;
    private String customerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double monthlyRent;
    private double totalValue; // Doanh thu tổng của hợp đồng
    private String notes;

    public LeaseContract(int contractId, int apartmentId, String customerName, LocalDate startDate, LocalDate endDate, double monthlyRent, String notes) {
        this.contractId = contractId;
        this.apartmentId = apartmentId;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.notes = notes;
        this.totalValue = calculateTotalValue();
    }

    public LeaseContract(int contractId, int apartmentId, String customerName, LocalDate startDate, LocalDate endDate, double monthlyRent) {
        this(contractId, apartmentId, customerName, startDate, endDate, monthlyRent, "");
    }

    // Hàm tính tổng doanh thu dựa trên số tháng thuê
    private double calculateTotalValue() {
        if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
            // Tính số tháng (đơn giản hóa)
            int months = (endDate.getYear() - startDate.getYear()) * 12 + 
                         (endDate.getMonthValue() - startDate.getMonthValue());
            if (months <= 0) months = 1; // Tối thiểu 1 tháng
            return months * monthlyRent;
        }
        return 0;
    }

    // ==========================================
    // CÁC HÀM GETTER/SETTER (Sửa lỗi undefined)
    // ==========================================
    
    public int getContractId() { return contractId; }
    public void setContractId(int contractId) { this.contractId = contractId; }

    public int getApartmentId() { return apartmentId; }
    public void setApartmentId(int apartmentId) { this.apartmentId = apartmentId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
        this.totalValue = calculateTotalValue();
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
        this.totalValue = calculateTotalValue();
    }

    public double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(double monthlyRent) { 
        this.monthlyRent = monthlyRent; 
        this.totalValue = calculateTotalValue();
    }

    public double getTotalValue() { return totalValue; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}