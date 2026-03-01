package com.tradingdemo.dao;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.service.IPInfoService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LoginAuditDAO - Data access object for login audit records
 */
public class LoginAuditDAO {

    /**
     * Record a login attempt with IP information
     * @param userId The user ID
     * @param ipInfo The IP information from IPInfoService
     * @return true if insertion was successful
     */
    public boolean recordLogin(int userId, IPInfoService.IPInfo ipInfo) {
        String sql = "INSERT INTO login_audit (user_id, ip_address, country, city, region, isp, timezone, login_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, ipInfo != null ? ipInfo.ip : "N/A");
            ps.setString(3, ipInfo != null ? ipInfo.country : "N/A");
            ps.setString(4, ipInfo != null ? ipInfo.city : "N/A");
            ps.setString(5, ipInfo != null ? ipInfo.region : "N/A");
            ps.setString(6, ipInfo != null ? ipInfo.isp : "N/A");
            ps.setString(7, ipInfo != null ? ipInfo.timezone : "N/A");
            
            int rowsInserted = ps.executeUpdate();
            System.out.println("Login audit recorded for user " + userId + " from " + 
                              (ipInfo != null ? ipInfo.city + ", " + ipInfo.country : "Unknown"));
            return rowsInserted > 0;
        } catch (Exception e) {
            System.err.println("Failed to record login audit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get login history for a specific user
     * @param userId The user ID
     * @param limit Maximum number of records to retrieve (e.g., 10)
     * @return List of login records as formatted strings
     */
    public List<String> getLoginHistory(int userId, int limit) {
        String sql = "SELECT ip_address, country, city, isp, login_time FROM login_audit " +
                     "WHERE user_id = ? ORDER BY login_time DESC LIMIT ?";
        List<String> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String record = String.format("%s | %s, %s | ISP: %s | %s",
                        rs.getString("ip_address"),
                        rs.getString("city"),
                        rs.getString("country"),
                        rs.getString("isp"),
                        rs.getTimestamp("login_time")
                    );
                    history.add(record);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve login history: " + e.getMessage());
        }
        return history;
    }

    /**
     * Get the most recent login for a user
     * @param userId The user ID
     * @return IPInfo representing the most recent login, or null if not found
     */
    public IPInfoService.IPInfo getMostRecentLogin(int userId) {
        String sql = "SELECT ip_address, country, city, region, isp, timezone FROM login_audit " +
                     "WHERE user_id = ? ORDER BY login_time DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    IPInfoService.IPInfo info = new IPInfoService.IPInfo();
                    info.ip = rs.getString("ip_address");
                    info.country = rs.getString("country");
                    info.city = rs.getString("city");
                    info.region = rs.getString("region");
                    info.isp = rs.getString("isp");
                    info.timezone = rs.getString("timezone");
                    return info;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve most recent login: " + e.getMessage());
        }
        return null;
    }
}
