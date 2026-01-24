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
import com.tradingdemo.model.Order;

/**
 * OrderDAO - Data Access Object for Order entity
 * Handles all database operations for trading orders
 */
public class OrderDAO {

    private final Connection connection;

    public OrderDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Create a new order
     * @param order The order to create
     * @return true if successful, false otherwise
     */
    public boolean createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, symbol, type, side, price, quantity, status, stop_loss, take_profit, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUserId());
            stmt.setString(2, order.getSymbol());
            stmt.setString(3, order.getType());
            stmt.setString(4, order.getSide());
            stmt.setDouble(5, order.getPrice());
            stmt.setDouble(6, order.getQuantity());
            stmt.setString(7, order.getStatus());
            stmt.setDouble(8, order.getStopLoss());
            stmt.setDouble(9, order.getTakeProfit());
            stmt.setTimestamp(10, Timestamp.valueOf(order.getCreatedAt()));
            
            System.out.println("DEBUG [OrderDAO.createOrder]: Saving order - SL=" + order.getStopLoss() + ", TP=" + order.getTakeProfit());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get an order by ID
     * @param orderId The order ID
     * @return Order object or null if not found
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving order by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all orders for a specific user
     * @param userId The user ID
     * @return List of orders
     */
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving orders by user: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Get all pending orders for a user
     * @param userId The user ID
     * @return List of pending orders
     */
    public List<Order> getPendingOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? AND status = 'PENDING' ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pending orders: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Update an order
     * @param order The order to update
     * @return true if successful, false otherwise
     */
    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET symbol = ?, type = ?, side = ?, price = ?, quantity = ?, status = ?, stop_loss = ?, take_profit = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, order.getSymbol());
            stmt.setString(2, order.getType());
            stmt.setString(3, order.getSide());
            stmt.setDouble(4, order.getPrice());
            stmt.setDouble(5, order.getQuantity());
            stmt.setString(6, order.getStatus());
            stmt.setDouble(7, order.getStopLoss());
            stmt.setDouble(8, order.getTakeProfit());
            stmt.setInt(9, order.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete an order
     * @param orderId The order ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
        return false;
    }

    /**
     * Helper method to map ResultSet row to Order object
     * @param rs The ResultSet
     * @return Order object
     * @throws SQLException
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setSymbol(rs.getString("symbol"));
        order.setType(rs.getString("type"));
        order.setSide(rs.getString("side"));
        order.setPrice(rs.getDouble("price"));
        order.setQuantity(rs.getDouble("quantity"));
        order.setStatus(rs.getString("status"));
        order.setStopLoss(rs.getDouble("stop_loss"));
        order.setTakeProfit(rs.getDouble("take_profit"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            order.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        return order;
    }
}
