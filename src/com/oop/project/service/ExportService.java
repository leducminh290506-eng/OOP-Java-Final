package com.oop.project.service;

import com.oop.project.model.Apartment;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportService {
    
    public void exportToCSV(List<Apartment> apartments, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("ID,Title,Address,Price,Area,Type,Status,Favorite\n");
            for (Apartment apt : apartments) {
                writer.append(String.valueOf(apt.getId())).append(",")
                      .append(apt.getTitle()).append(",")
                      .append(apt.getAddress()).append(",")
                      .append(String.valueOf(apt.getPrice())).append(",")
                      .append(String.valueOf(apt.getArea())).append(",")
                      .append(apt.getType().name()).append(",")
                      .append(apt.getStatus()).append(",")
                      .append(String.valueOf(apt.isFavorite())).append("\n");
            }
        }
    }
}