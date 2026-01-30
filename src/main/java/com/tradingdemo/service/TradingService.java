package com.tradingdemo.service;

import java.util.List;
import java.util.Random;

import com.tradingdemo.dao.OrderDAO;
import com.tradingdemo.dao.TradeDAO;
import com.tradingdemo.model.Order;
import com.tradingdemo.model.Trade;

/**
 * TradingService - Business logic for trading operations
 * Handles order placement, execution, and trade history
 */
public class TradingService {

    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final WalletService walletService;
    private final Random random = new Random();

    public TradingService() {
        this.orderDAO = new OrderDAO();
        this.tradeDAO = new TradeDAO();
        this.walletService = new WalletService();
    }

    /**
     * Places a new trading order
     * @param userId The user ID
     * @param symbol Cryptocurrency symbol
     * @param type Order type (MARKET or LIMIT)
     * @param side Order side (BUY or SELL)
     * @param price Price per unit
     * @param quantity Quantity to buy/sell
     * @param stopLoss Stop-loss price
     * @param takeProfit Take-profit price
     * @return The created Order object or null if failed
     */
    public Order placeOrder(int userId, String symbol, String type, String side, 
                           double price, double quantity, double stopLoss, double takeProfit) {
        System.out.println("DEBUG [TradingService.placeOrder]: Creating order with SL=" + stopLoss + ", TP=" + takeProfit);
        Order order = new Order(userId, symbol, type, side, price, quantity, stopLoss, takeProfit);
        System.out.println("DEBUG [TradingService.placeOrder]: Order created - SL=" + order.getStopLoss() + ", TP=" + order.getTakeProfit());
        
        if (orderDAO.createOrder(order)) {
            // Auto-execute market orders
            if ("MARKET".equals(type)) {
                executeOrder(order, price);
            }
            return order;
        }
        
        return null;
    }

    /**
     * Executes a pending order
     * Both BUY (LONG) and SELL (SHORT) require spending money as margin
     * @param order The order to execute
     * @param executionPrice The price at which order is executed
     * @return true if successful, false otherwise
     */
    public boolean executeOrder(Order order, double executionPrice) {
        // Calculate margin cost (position value)
        double marginCost = executionPrice * order.getQuantity();
        
        // Check if user has enough balance
        double balance = walletService.getUserBalance(order.getUserId());
        if (balance < marginCost) {
            System.err.println("Insufficient balance for order: " + marginCost + " > " + balance);
            return false;
        }
        
        // Deduct margin from balance for BOTH BUY (long) and SELL (short)
        walletService.deductBalance(order.getUserId(), marginCost);
        
        // Update order status
        order.setStatus("EXECUTED");
        orderDAO.updateOrder(order);

        // Create trade record
        Trade trade = new Trade(order.getId(), order.getSymbol(), order.getSide(), 
                               executionPrice, order.getQuantity());
        
        if (tradeDAO.createTrade(trade)) {
            // Add position to wallet (for tracking, not actual crypto)
            walletService.addToWallet(order.getUserId(), order.getSymbol(), 
                                    order.getQuantity(), executionPrice);
            return true;
        }
        
        return false;
    }

    /**
     * Cancels a pending order and refunds the margin
     * @param orderId The order ID to cancel
     * @return true if successful, false otherwise
     */
    public boolean cancelOrder(int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            return orderDAO.updateOrder(order);
        }
        return false;
    }
    
    /**
     * Closes an executed order and calculates profit/loss
     * For LONG (BUY): profit = (currentPrice - entryPrice) * quantity
     * For SHORT (SELL): profit = (entryPrice - currentPrice) * quantity
     * @param orderId The order ID to close
     * @param currentPrice The current market price
     * @return The profit/loss amount, or null if failed
     */
    public Double closePosition(int orderId, double currentPrice) {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null || !"EXECUTED".equals(order.getStatus())) {
            return null;
        }
        
        double entryPrice = order.getPrice();
        double quantity = order.getQuantity();
        double margin = entryPrice * quantity;
        double profitLoss;
        
        if ("BUY".equals(order.getSide())) {
            // LONG position: profit when price goes UP
            profitLoss = (currentPrice - entryPrice) * quantity;
        } else {
            // SHORT position: profit when price goes DOWN
            profitLoss = (entryPrice - currentPrice) * quantity;
        }
        
        // Return margin + profit (or margin - loss)
        double totalReturn = margin + profitLoss;
        walletService.addBalance(order.getUserId(), totalReturn);
        
        // Remove position from wallet
        walletService.removeFromWallet(order.getUserId(), order.getSymbol(), quantity);
        
        // Update order status
        order.setStatus("CLOSED");
        orderDAO.updateOrder(order);
        
        // Create closing trade record
        Trade closeTrade = new Trade(order.getId(), order.getSymbol(), 
                                     "BUY".equals(order.getSide()) ? "CLOSE_LONG" : "CLOSE_SHORT",
                                     currentPrice, quantity);
        tradeDAO.createTrade(closeTrade);
        
        return profitLoss;
    }

    /**
     * Gets order history for a user
     * @param userId The user ID
     * @return List of user's orders
     */
    public List<Order> getOrderHistory(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    /**
     * Gets trade history for a user
     * @param userId The user ID
     * @return List of user's trades
     */
    public List<Trade> getTradeHistory(int userId) {
        return tradeDAO.getTradesByUserId(userId);
    }

    /**
     * Gets pending orders for a user
     * @param userId The user ID
     * @return List of pending orders
     */
    public List<Order> getPendingOrders(int userId) {
        return orderDAO.getPendingOrdersByUserId(userId);
    }

    /**
     * Gets all orders for a user (both PENDING and EXECUTED)
     * @param userId The user ID
     * @return List of all user orders
     */
    public List<Order> getAllOrdersForUser(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    /**
     * Simulates price movement and checks stop-loss/take-profit triggers
     * @param currentPrice Current market price
     * @param order The order to check
     * @return true if triggered, false otherwise
     */
    public boolean checkStopLossTakeProfit(double currentPrice, Order order) {
        if ("BUY".equals(order.getSide())) {
            // Stop-loss for buy orders (price goes down)
            if (order.getStopLoss() > 0 && currentPrice <= order.getStopLoss()) {
                return true;
            }
            // Take-profit for buy orders (price goes up)
            if (order.getTakeProfit() > 0 && currentPrice >= order.getTakeProfit()) {
                return true;
            }
        } else if ("SELL".equals(order.getSide())) {
            // Stop-loss for sell orders (price goes up)
            if (order.getStopLoss() > 0 && currentPrice >= order.getStopLoss()) {
                return true;
            }
            // Take-profit for sell orders (price goes down)
            if (order.getTakeProfit() > 0 && currentPrice <= order.getTakeProfit()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a specific order details
     * @param orderId The order ID
     * @return Order object or null if not found
     */
    public Order getOrderDetails(int orderId) {
        return orderDAO.getOrderById(orderId);
    }

    /**
     * Simulates a random market price for a cryptocurrency
     * @param basePrice Base price to simulate around
     * @return Simulated price
     */
    public double simulateMarketPrice(double basePrice) {
        double variance = basePrice * 0.05; // 5% variance
        double randomChange = (random.nextDouble() - 0.5) * 2 * variance;
        return Math.max(basePrice + randomChange, 1.0); // Ensure positive price
    }
}
