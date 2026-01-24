package com.tradingdemo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.model.WalletItem;

/**
 * WalletDAO - Data Access Object for WalletItem entity
 * Handles all database operations for wallet items
 */
public class WalletDAO {

    private final Connection connection;

    public WalletDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Add an item to user's wallet
     * @param walletItem The wallet item to add
     * @return true if successful, false otherwise
     */
    public boolean addWalletItem(WalletItem walletItem) {
        String sql = "INSERT INTO wallet_items (user_id, symbol, quantity, average_price) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, walletItem.getUserId());
            stmt.setString(2, walletItem.getSymbol());
            stmt.setDouble(3, walletItem.getQuantity());
            stmt.setDouble(4, walletItem.getAveragePrice());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        walletItem.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding wallet item: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get wallet items for a specific user
     * @param userId The user ID
     * @return List of wallet items
     */
    public List<WalletItem> getWalletByUserId(int userId) {
        List<WalletItem> items = new ArrayList<>();
        String sql = "SELECT * FROM wallet_items WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(mapResultSetToWalletItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving wallet: " + e.getMessage());
        }
        return items;
    }

    /**
     * Get a specific wallet item
     * @param itemId The wallet item ID
     * @return WalletItem object or null if not found
     */
    public WalletItem getWalletItemById(int itemId) {
        String sql = "SELECT * FROM wallet_items WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToWalletItem(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving wallet item: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get a specific wallet item by user and symbol
     * @param userId The user ID
     * @param symbol The cryptocurrency symbol
     * @return WalletItem object or null if not found
     */
    public WalletItem getWalletItemByUserAndSymbol(int userId, String symbol) {
        String sql = "SELECT * FROM wallet_items WHERE user_id = ? AND symbol = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, symbol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToWalletItem(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving wallet item by user and symbol: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update a wallet item (e.g., after buying/selling)
     * @param walletItem The wallet item to update
     * @return true if successful, false otherwise
     */
    public boolean updateWalletItem(WalletItem walletItem) {
        String sql = "UPDATE wallet_items SET quantity = ?, average_price = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, walletItem.getQuantity());
            stmt.setDouble(2, walletItem.getAveragePrice());
            stmt.setInt(3, walletItem.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating wallet item: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a wallet item
     * @param itemId The wallet item ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteWalletItem(int itemId) {
        String sql = "DELETE FROM wallet_items WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting wallet item: " + e.getMessage());
        }
        return false;
    }

    /**
     * Helper method to map ResultSet row to WalletItem object
     * @param rs The ResultSet
     * @return WalletItem object
     * @throws SQLException
     */
    private WalletItem mapResultSetToWalletItem(ResultSet rs) throws SQLException {
        WalletItem item = new WalletItem();
        item.setId(rs.getInt("id"));
        item.setUserId(rs.getInt("user_id"));
        item.setSymbol(rs.getString("symbol"));
        item.setQuantity(rs.getDouble("quantity"));
        item.setAveragePrice(rs.getDouble("average_price"));
        return item;
    }
}
