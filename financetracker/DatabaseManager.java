package com.example.financetracker;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/finance_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    //returns connection
    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD); // Attempt to connect to the database
        } catch (SQLException e) {
            e.printStackTrace(); // Print any exceptions that occur
            return null; // If there's an error, return null to indicate failure
        }
    }
}

