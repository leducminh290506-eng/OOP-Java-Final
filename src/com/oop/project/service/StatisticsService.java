package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.model.ApartmentType;
import com.oop.project.repository.ApartmentRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {
    private final ApartmentRepository repository;

    public StatisticsService() {
        this.repository = new ApartmentRepository();
    }

    public int getTotalApartments() {
        return repository.findAll().size();
    }

    public Map<ApartmentType, Long> getCountByType() {
        List<Apartment> all = repository.findAll();
        return all.stream().collect(Collectors.groupingBy(Apartment::getType, Collectors.counting()));
    }

    public double getAveragePrice() {
        List<Apartment> all = repository.findAll();
        return all.stream().mapToDouble(Apartment::getPrice).average().orElse(0.0);
    }
}