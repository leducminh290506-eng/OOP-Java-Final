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
                  .collect(Collectors.groupingBy(Apartment::getLocation, Collectors.counting()));
    }
}