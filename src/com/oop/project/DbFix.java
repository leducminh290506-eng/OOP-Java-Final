package com.oop.project;

import com.oop.project.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DbFix {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            try {
                stmt.execute("ALTER TABLE apartments CHANGE size_sqft size_m2 INT");
                System.out.println("Changed size_sqft to size_m2");
            } catch (Exception e) {
                System.out.println("Error changing size_m2: " + e.getMessage());
            }

            try {
                stmt.execute("ALTER TABLE lease_contracts ADD COLUMN notes TEXT");
                System.out.println("Added notes to lease_contracts");
            } catch (Exception e) {
                System.out.println("Error adding notes: " + e.getMessage());
            }

            try {
                stmt.execute("ALTER TABLE apartments ADD COLUMN description TEXT");
                System.out.println("Added description to apartments");
            } catch (Exception e) {
                System.out.println("Error adding description: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
