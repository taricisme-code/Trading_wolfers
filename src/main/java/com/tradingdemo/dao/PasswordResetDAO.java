package com.tradingdemo.dao;

import com.tradingdemo.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetDAO {
    private final Connection connection;

    public PasswordResetDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Creates a reset token (code) for a user. Expires in 15 minutes.
     */
    public boolean createResetToken(int userId, String code) {
        String sql = "INSERT INTO password_resets (user_id, token, expires_at, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, code);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating reset token: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verify a token for a given user id (and that it has not expired)
     */
    public boolean verifyTokenForUser(int userId, String token) {
        String sql = "SELECT * FROM password_resets WHERE user_id = ? AND token = ? AND expires_at > ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, token);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error verifying reset token: " + e.getMessage());
        }
        return false;
    }

    /**
     * Consume (delete) a token after use
     */
    public boolean consumeToken(int userId, String token) {
        String sql = "DELETE FROM password_resets WHERE user_id = ? AND token = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, token);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error consuming reset token: " + e.getMessage());
        }
        return false;
    }
}
