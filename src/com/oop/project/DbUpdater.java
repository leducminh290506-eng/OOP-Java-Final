package com.oop.project;

import com.oop.project.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DbUpdater {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE apartments ADD COLUMN description TEXT;");
            System.out.println("Column description added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
