package com.oop.project.service;

import com.oop.project.model.Apartment;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ExportService {
    
    public void exportToCSV(List<Apartment> apartments, String filePath) throws IOException {
        exportToCSV(apartments, filePath, a -> Collections.emptyList());
    }

    public interface AmenityProvider {
        List<String> getAmenityNames(Apartment apartment);
    }

    public void exportToCSV(List<Apartment> apartments, String filePath, AmenityProvider amenityProvider) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("ID,ListingCode,Address,Location,Price,Bedrooms,AreaSqft,Type,Category,Amenities\n");
            for (Apartment apt : apartments) {
                List<String> amenities = (amenityProvider == null) ? Collections.emptyList() : amenityProvider.getAmenityNames(apt);
                String amenityJoined = String.join(" | ", amenities);

                writer.append(String.valueOf(apt.getId())).append(",")
                        .append(csv(apt.getListingCode())).append(",")
                        .append(csv(apt.getAddress())).append(",")
                        .append(csv(apt.getLocation())).append(",")
                        .append(String.valueOf(apt.getPrice())).append(",")
                        .append(String.valueOf(apt.getBedrooms())).append(",")
                        .append(String.valueOf(apt.getArea())).append(",")
                        .append(csv(apt.getType().name())).append(",")
                        .append(csv(apt.getCategory())).append(",")
                        .append(csv(amenityJoined))
                        .append("\n");
            }
        }
    }

    private String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}