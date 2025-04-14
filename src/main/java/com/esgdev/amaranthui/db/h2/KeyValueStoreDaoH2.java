package com.esgdev.amaranthui.db.h2;

import java.sql.*;
import java.util.logging.Logger;

public class KeyValueStoreDaoH2 {
    private static final String TABLE_NAME = "key_value_store";
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final Logger logger = Logger.getLogger(KeyValueStoreDaoH2.class.getName());

    public KeyValueStoreDaoH2(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    private void initializeDatabase() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS key_value_store (
                    store_key VARCHAR PRIMARY KEY,
                    store_value TEXT NOT NULL,
                    last_updated TIMESTAMP NOT NULL
                );
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize key_value_store table", e);
        }
    }

    public boolean saveValue(String key, String value) {
        String sql = """
                MERGE INTO key_value_store (store_key, store_value, last_updated)
                VALUES (?, ?, ?);
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setTimestamp(3, now);
            stmt.executeUpdate();
            return true; // Success
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to save value", e);
            return false; // Failure
        }
    }

    public String getValue(String key) {
        String sql = "SELECT store_value FROM key_value_store WHERE store_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("store_value");
                }
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to retrieve value", e);
        }
        return null;
    }
}