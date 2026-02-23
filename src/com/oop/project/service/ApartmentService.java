package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.repository.ApartmentRepository;
import java.util.List;

public class ApartmentService {
    private final ApartmentRepository repository;

    public ApartmentService(ApartmentRepository repository) {
        this.repository = repository;
    }

    public List<Apartment> getAllApartments() { return repository.findAll(); }
    public List<Apartment> filterByPrice(double min, double max) { return repository.findByPriceRange(min, max); }
    public void deleteApartment(int id) { repository.delete(id); }
    public List<Apartment> getFavorites(int userId) { return repository.findFavorites(userId); }
    public void toggleFavorite(int userId, int apartmentId) {}
}