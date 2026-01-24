package com.tradingdemo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.model.Prediction;

/**
 * PredictionDAO - Data Access Object for Prediction entity
 * Handles all database operations for price predictions
 */
public class PredictionDAO {

    private final Connection connection;

    public PredictionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createPrediction(Prediction prediction) {
        String sql = "INSERT INTO predictions (symbol, signal, confidence, target_price, analysis) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, prediction.getSymbol());
            stmt.setString(2, prediction.getSignal());
            stmt.setDouble(3, prediction.getConfidence());
            stmt.setDouble(4, prediction.getTargetPrice());
            stmt.setString(5, prediction.getAnalysis());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        prediction.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating prediction: " + e.getMessage());
        }
        return false;
    }

    public Prediction getPredictionById(int predictionId) {
        String sql = "SELECT * FROM predictions WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, predictionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPrediction(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving prediction: " + e.getMessage());
        }
        return null;
    }

    public Prediction getLatestPredictionBySymbol(String symbol) {
        String sql = "SELECT * FROM predictions WHERE symbol = ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, symbol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPrediction(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving latest prediction: " + e.getMessage());
        }
        return null;
    }

    public List<Prediction> getAllPredictions() {
        List<Prediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM predictions ORDER BY id DESC LIMIT 50";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                predictions.add(mapResultSetToPrediction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all predictions: " + e.getMessage());
        }
        return predictions;
    }

    public List<Prediction> getPredictionsBySymbol(String symbol) {
        List<Prediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM predictions WHERE symbol = ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, symbol);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                predictions.add(mapResultSetToPrediction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving predictions by symbol: " + e.getMessage());
        }
        return predictions;
    }

    private Prediction mapResultSetToPrediction(ResultSet rs) throws SQLException {
        Prediction prediction = new Prediction();
        prediction.setId(rs.getInt("id"));
        prediction.setSymbol(rs.getString("symbol"));
        prediction.setSignal(rs.getString("signal"));
        prediction.setConfidence(rs.getDouble("confidence"));
        prediction.setTargetPrice(rs.getDouble("target_price"));
        prediction.setAnalysis(rs.getString("analysis"));
        return prediction;
    }
}
