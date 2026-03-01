package com.tradingdemo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Singleton
 * Manages a single database connection throughout the application lifecycle.
 * Uses JDBC to connect to MySQL database.
 */
public class DatabaseConnection {

    // Database configuration
    // For XAMPP (phpMyAdmin) local MySQL
    // Default root has empty password. Update if you've set a password.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/crypto_trading_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // XAMPP default: empty password
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Singleton instance
    private static DatabaseConnection instance;
    private Connection connection;

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName(DB_DRIVER);
            // Establish connection
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Gets the singleton instance of DatabaseConnection
     * @return The single DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Gets the active database connection
     * @return The Connection object
     */
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving connection: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed!");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
