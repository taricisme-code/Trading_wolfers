package com.tradingdemo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.model.Trade;

/**
 * TradeDAO - Data Access Object for Trade entity
 * Handles all database operations for executed trades
 */
public class TradeDAO {

    private final Connection connection;

    public TradeDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Create a new trade (executed order)
     * @param trade The trade to create
     * @return true if successful, false otherwise
     */
    public boolean createTrade(Trade trade) {
        String sql = "INSERT INTO trades (order_id, symbol, side, executed_price, quantity, executed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, trade.getOrderId());
            stmt.setString(2, trade.getSymbol());
            stmt.setString(3, trade.getSide());
            stmt.setDouble(4, trade.getExecutedPrice());
            stmt.setDouble(5, trade.getQuantity());
            stmt.setTimestamp(6, Timestamp.valueOf(trade.getExecutedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        trade.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating trade: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get a trade by ID
     * @param tradeId The trade ID
     * @return Trade object or null if not found
     */
    public Trade getTradeById(int tradeId) {
        String sql = "SELECT * FROM trades WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tradeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTrade(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving trade by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all trades for a specific order
     * @param orderId The order ID
     * @return List of trades
     */
    public List<Trade> getTradesByOrderId(int orderId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades WHERE order_id = ? ORDER BY executed_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving trades by order: " + e.getMessage());
        }
        return trades;
    }

    /**
     * Get all trades for a specific user (via order)
     * @param userId The user ID
     * @return List of trades
     */
    public List<Trade> getTradesByUserId(int userId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT t.* FROM trades t " +
                     "JOIN orders o ON t.order_id = o.id " +
                     "WHERE o.user_id = ? ORDER BY t.executed_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving trades by user: " + e.getMessage());
        }
        return trades;
    }

    /**
     * Get all trades from the system (for admin)
     * @return List of all trades
     */
    public List<Trade> getAllTrades() {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades ORDER BY executed_at DESC LIMIT 100";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all trades: " + e.getMessage());
        }
        return trades;
    }

    /**
     * Get all trades for a specific symbol
     * @param symbol The cryptocurrency symbol
     * @return List of trades
     */
    public List<Trade> getTradesBySymbol(String symbol) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades WHERE symbol = ? ORDER BY executed_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, symbol);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving trades by symbol: " + e.getMessage());
        }
        return trades;
    }

    /**
     * Delete a trade
     * @param tradeId The trade ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteTrade(int tradeId) {
        String sql = "DELETE FROM trades WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tradeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting trade: " + e.getMessage());
        }
        return false;
    }

    /**
     * Helper method to map ResultSet row to Trade object
     * @param rs The ResultSet
     * @return Trade object
     * @throws SQLException
     */
    private Trade mapResultSetToTrade(ResultSet rs) throws SQLException {
        Trade trade = new Trade();
        trade.setId(rs.getInt("id"));
        trade.setOrderId(rs.getInt("order_id"));
        trade.setSymbol(rs.getString("symbol"));
        trade.setSide(rs.getString("side"));
        trade.setExecutedPrice(rs.getDouble("executed_price"));
        trade.setQuantity(rs.getDouble("quantity"));
        
        Timestamp executedTs = rs.getTimestamp("executed_at");
        if (executedTs != null) {
            trade.setExecutedAt(executedTs.toLocalDateTime());
        }
        
        return trade;
    }
}
