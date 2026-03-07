package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("PostgreSQL JDBC driver not found", ex);
        }
    }

    private Db() {}

    public static Connection getConnection() throws SQLException {
        String url = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/letmedrive");
        String user = getEnvOrDefault("DB_USER", "postgres");
        String password = getEnvOrDefault("DB_PASSWORD", "root");
        return DriverManager.getConnection(url, user, password);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
