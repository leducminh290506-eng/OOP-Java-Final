package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.repository.ContractRepository;
import com.oop.project.util.DatabaseConnection;
import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {
    private final ApartmentRepository apartmentRepo;
    private final ContractRepository contractRepo;

    public StatisticsService() {
        this.apartmentRepo = new ApartmentRepository();
        this.contractRepo = new ContractRepository();
    }

    public int getTotalApartments() {
        return apartmentRepo.findAll().size();
    }

    public double getOccupancyRate() {
        try {
            int total = getTotalApartments();
            if (total == 0) return 0.0;
            int active = contractRepo.countActiveContracts();
            return ((double) active / total) * 100;
        } catch (SQLException e) { return 0.0; }
    }

    public int getActiveContractsCount() {
        try {
            return contractRepo.countActiveContracts();
        } catch (SQLException e) { return 0; }
    }

    public double getEstimatedMonthlyRevenue() {
        try {
            return contractRepo.getTotalMonthlyRevenue();
        } catch (SQLException e) { return 0.0; }
    }

    public Map<String, Long> getCountByLocation() {
        return apartmentRepo.findAll().stream()
                  .collect(Collectors.groupingBy(a -> {
                      String loc = a.getLocation();
                      if (loc == null || loc.isBlank()) return "Unknown";
                      int comma = loc.lastIndexOf(',');
                      return (comma >= 0) ? loc.substring(comma + 1).trim() : loc.trim();
                  }, Collectors.counting()));
    }

    /** FR-5.4: Average price across all apartments */
    public double getAveragePrice() {
        return apartmentRepo.findAll().stream()
            .mapToDouble(Apartment::getPrice)
            .average()
            .orElse(0.0);
    }

    /** FR-5.4: Total number of favorited entries across all users */
    public int getFavoritesCount() {
        try {
            String sql = "SELECT COUNT(*) FROM favorites";
            try (Connection conn = com.oop.project.util.DatabaseConnection.getInstance().getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { return 0; }
        return 0;
    }
}