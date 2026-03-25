package com.github.diszexuf.server.db;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:messages.db";
    private static DatabaseManager instance;
    private final Connection connection;

    private DatabaseManager() throws SQLException {
        this.connection = DriverManager.getConnection(URL);
        initSchema();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Connection was closed");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }));
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null)
            instance = new DatabaseManager();
        return instance;
    }

    private void initSchema() throws SQLException {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS messages(" +
                "id INTEGER PRIMARY KEY," +
                "\"time\" TEXT," +
                "\"user\" TEXT," +
                "\"text\" TEXT," +
                "result INTEGER)";
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(sqlCreateTable);
        }
    }

    public synchronized void saveMessage(
            LocalDateTime time, String user, String text, int result
    ) throws SQLException {
        String sql = "INSERT INTO MESSAGES (\"time\", \"user\", \"text\", result) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, time.toString());
            ps.setString(2, user);
            ps.setString(3, text);
            ps.setInt(4, result);
            ps.executeUpdate();
        }
    }
}
