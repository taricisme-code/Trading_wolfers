package com.tradingdemo.dao;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.model.AlertRule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {
    private final Connection connection;

    public AlertDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createAlert(AlertRule a) {
        String sql = "INSERT INTO alerts (user_id, symbol, target_price, notify_when_above, enabled, notify_email, notify_sms, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, a.getUserId());
            stmt.setString(2, a.getSymbol());
            stmt.setDouble(3, a.getTargetPrice());
            stmt.setBoolean(4, a.isNotifyWhenAbove());
            stmt.setBoolean(5, a.isEnabled());
            stmt.setBoolean(6, a.isNotifyEmail());
            stmt.setBoolean(7, a.isNotifySms());
            stmt.setTimestamp(8, Timestamp.valueOf(a.getCreatedAt()));
            int r = stmt.executeUpdate();
            if (r > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) { if (rs.next()) a.setId(rs.getInt(1)); }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating alert: " + e.getMessage());
        }
        return false;
    }

    public List<AlertRule> getAlertsByUser(int userId) {
        List<AlertRule> out = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                out.add(map(rs));
            }
        } catch (SQLException e) { System.err.println("Error reading alerts: " + e.getMessage()); }
        return out;
    }

    public List<AlertRule> getActiveAlerts() {
        List<AlertRule> out = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE enabled = TRUE";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { System.err.println("Error reading active alerts: " + e.getMessage()); }
        return out;
    }

    public boolean updateAlert(AlertRule a) {
        String sql = "UPDATE alerts SET symbol=?, target_price=?, notify_when_above=?, enabled=?, notify_email=?, notify_sms=? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, a.getSymbol());
            stmt.setDouble(2, a.getTargetPrice());
            stmt.setBoolean(3, a.isNotifyWhenAbove());
            stmt.setBoolean(4, a.isEnabled());
            stmt.setBoolean(5, a.isNotifyEmail());
            stmt.setBoolean(6, a.isNotifySms());
            stmt.setInt(7, a.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error updating alert: " + e.getMessage()); }
        return false;
    }

    public boolean deleteAlert(int id) {
        String sql = "DELETE FROM alerts WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting alert: " + e.getMessage());
        }
        return false;
    }

    private AlertRule map(ResultSet rs) throws SQLException {
        AlertRule a = new AlertRule();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setSymbol(rs.getString("symbol"));
        a.setTargetPrice(rs.getDouble("target_price"));
        a.setNotifyWhenAbove(rs.getBoolean("notify_when_above"));
        a.setEnabled(rs.getBoolean("enabled"));
        a.setNotifyEmail(rs.getBoolean("notify_email"));
        a.setNotifySms(rs.getBoolean("notify_sms"));
        Timestamp t = rs.getTimestamp("created_at"); if (t != null) a.setCreatedAt(t.toLocalDateTime());
        return a;
    }
}
