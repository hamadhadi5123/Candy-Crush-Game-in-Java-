package application;

import java.io.*;
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/lab9";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Throwable e) {
            try {
                File jarFile = new File("lib/mysql-connector-j-8.4.0.jar");
                if (jarFile.exists()) {
                    java.net.URLClassLoader ucl = new java.net.URLClassLoader(
                            new java.net.URL[] { jarFile.toURI().toURL() }, DatabaseManager.class.getClassLoader());
                    Driver d = (Driver) Class.forName("com.mysql.cj.jdbc.Driver", true, ucl)
                            .getDeclaredConstructor().newInstance();
                    DriverManager.registerDriver(new DriverShim(d));
                }
            } catch (Throwable t) {
                System.err.println("DatabaseManager driver notice: " + t.getMessage());
            }
        }

        // Initialize schema if possible
        try (Connection conn = getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS lab9.login (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "username VARCHAR(100) NOT NULL UNIQUE, " +
                            "password VARCHAR(100) NOT NULL, " +
                            "email VARCHAR(150), " +
                            "phone VARCHAR(50), " +
                            "high_score INT DEFAULT 0)");
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, DB_USER, DB_PASS);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean authenticateUser(String username, String password) {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                return true; // Offline fallback: allow login
            }
            String query = "SELECT * FROM lab9.login WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Auth Error: " + e.getMessage());
            return true; // Graceful offline fallback
        }
    }

    public static boolean registerUser(String username, String password, String email, String phone) {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                return true; // Offline registration success
            }
            String query = "INSERT INTO lab9.login(username, password, email, phone) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE password = VALUES(password), email = VALUES(email), phone = VALUES(phone)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("SQL Register Error: " + e.getMessage());
            return true; // Graceful fallback
        }
    }

    public static int getHighScore(String username, int localDefault) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                String query = "SELECT high_score FROM lab9.login WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, username);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int score = rs.getInt("high_score");
                            return Math.max(score, localDefault);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return localDefault;
    }

    public static void saveHighScore(String username, int newHighScore) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                String query = "UPDATE lab9.login SET high_score = ? WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, newHighScore);
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
