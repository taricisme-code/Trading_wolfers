package com.tradingdemo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.model.Reclamation;

/**
 * ReclamationDAO - Data Access Object for Reclamation entity
 * Handles all database operations for user complaints/issues
 */
public class ReclamationDAO {

    private final Connection connection;

    public ReclamationDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createReclamation(Reclamation reclamation) {
        String sql = "INSERT INTO reclamations (user_id, title, content, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reclamation.getUserId());
            stmt.setString(2, reclamation.getTitle());
            stmt.setString(3, reclamation.getContent());
            stmt.setString(4, reclamation.getStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(reclamation.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.valueOf(reclamation.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reclamation.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating reclamation: " + e.getMessage());
        }
        return false;
    }

    public Reclamation getReclamationById(int reclamationId) {
        String sql = "SELECT * FROM reclamations WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReclamation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reclamation: " + e.getMessage());
        }
        return null;
    }

    public List<Reclamation> getReclamationsByUserId(int userId) {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamations WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reclamations.add(mapResultSetToReclamation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reclamations by user: " + e.getMessage());
        }
        return reclamations;
    }

    public List<Reclamation> getReclamationsByStatus(String status) {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamations WHERE status = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reclamations.add(mapResultSetToReclamation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reclamations by status: " + e.getMessage());
        }
        return reclamations;
    }

    public boolean updateReclamation(Reclamation reclamation) {
        String sql = "UPDATE reclamations SET title = ?, content = ?, status = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reclamation.getTitle());
            stmt.setString(2, reclamation.getContent());
            stmt.setString(3, reclamation.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, reclamation.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reclamation: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteReclamation(int reclamationId) {
        String sql = "DELETE FROM reclamations WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reclamationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting reclamation: " + e.getMessage());
        }
        return false;
    }

    private Reclamation mapResultSetToReclamation(ResultSet rs) throws SQLException {
        Reclamation reclamation = new Reclamation();
        reclamation.setId(rs.getInt("id"));
        reclamation.setUserId(rs.getInt("user_id"));
        reclamation.setTitle(rs.getString("title"));
        reclamation.setContent(rs.getString("content"));
        reclamation.setStatus(rs.getString("status"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            reclamation.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            reclamation.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        
        return reclamation;
    }
}
