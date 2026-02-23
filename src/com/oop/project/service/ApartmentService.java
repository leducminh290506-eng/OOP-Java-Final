package com.oop.project.service;

import com.oop.project.model.Apartment;
import com.oop.project.repository.ApartmentRepository;
import com.oop.project.util.ValidationUtil;

import java.util.List;

public class ApartmentService {
    private final ApartmentRepository repository;

    public ApartmentService() {
        this.repository = new ApartmentRepository();
    }

    public void addApartment(Apartment apt) {
        ValidationUtil.validateApartment(apt);
        repository.save(apt);
    }

    public void updateApartment(Apartment apt) {
        ValidationUtil.validateApartment(apt);
        repository.update(apt);
    }

    public void deleteApartment(int id) {
        repository.delete(id);
    }

    public List<Apartment> getAllApartments() {
        return repository.findAll();
    }

    public List<Apartment> filterByPrice(double min, double max) {
        return repository.findByPriceRange(min, max);
    }

    public void toggleFavorite(int id, boolean status) {
        repository.findById(id).ifPresent(apt -> {
            apt.setFavorite(status);
            repository.update(apt);
        });
    }

    public List<Apartment> getFavorites() {
        return repository.findFavorites();
    }
}