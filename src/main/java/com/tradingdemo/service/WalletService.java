package com.tradingdemo.service;

import java.util.List;

import com.tradingdemo.dao.UserDAO;
import com.tradingdemo.dao.WalletDAO;
import com.tradingdemo.model.User;
import com.tradingdemo.model.WalletItem;

/**
 * WalletService - Business logic for wallet management
 * Handles wallet operations (buy, sell, balance updates)
 */
public class WalletService {

    private final WalletDAO walletDAO;
    private final UserDAO userDAO;

    public WalletService() {
        this.walletDAO = new WalletDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Gets the wallet for a specific user
     * @param userId The user ID
     * @return List of wallet items
     */
    public List<WalletItem> getUserWallet(int userId) {
        return walletDAO.getWalletByUserId(userId);
    }

    /**
     * Gets total portfolio value for a user
     * @param userId The user ID
     * @param currentPrices Map of current cryptocurrency prices
     * @return Total portfolio value
     */
    public double getPortfolioValue(int userId, java.util.Map<String, Double> currentPrices) {
        List<WalletItem> items = getUserWallet(userId);
        double totalValue = 0;
        
        for (WalletItem item : items) {
            double price = currentPrices.getOrDefault(item.getSymbol(), 0.0);
            totalValue += item.getQuantity() * price;
        }
        
        return totalValue;
    }

    /**
     * Adds cryptocurrency to user's wallet after a buy order
     * @param userId The user ID
     * @param symbol Cryptocurrency symbol
     * @param quantity Quantity to add
     * @param price Price per unit
     * @return true if successful, false otherwise
     */
    public boolean addToWallet(int userId, String symbol, double quantity, double price) {
        WalletItem existing = walletDAO.getWalletItemByUserAndSymbol(userId, symbol);
        
        if (existing != null) {
            // Update existing holding - calculate new average price
            double totalQuantity = existing.getQuantity() + quantity;
            double totalCost = (existing.getQuantity() * existing.getAveragePrice()) + (quantity * price);
            double newAveragePrice = totalCost / totalQuantity;
            
            existing.setQuantity(totalQuantity);
            existing.setAveragePrice(newAveragePrice);
            return walletDAO.updateWalletItem(existing);
        } else {
            // Create new wallet item
            WalletItem newItem = new WalletItem(userId, symbol, quantity, price);
            return walletDAO.addWalletItem(newItem);
        }
    }

    /**
     * Removes cryptocurrency from user's wallet after a sell order
     * @param userId The user ID
     * @param symbol Cryptocurrency symbol
     * @param quantity Quantity to remove
     * @return true if successful, false otherwise
     */
    public boolean removeFromWallet(int userId, String symbol, double quantity) {
        WalletItem item = walletDAO.getWalletItemByUserAndSymbol(userId, symbol);
        
        if (item != null && item.getQuantity() >= quantity) {
            if (Math.abs(item.getQuantity() - quantity) < 0.0001) { // Selling all
                return walletDAO.deleteWalletItem(item.getId());
            } else {
                item.setQuantity(item.getQuantity() - quantity);
                return walletDAO.updateWalletItem(item);
            }
        }
        
        return false;
    }

    /**
     * Gets user's current balance
     * @param userId The user ID
     * @return The user's balance
     */
    public double getUserBalance(int userId) {
        User user = userDAO.getUserById(userId);
        return user != null ? user.getBalance() : 0;
    }

    /**
     * Updates user's balance (after buying/selling)
     * @param userId The user ID
     * @param newBalance The new balance
     * @return true if successful, false otherwise
     */
    public boolean updateUserBalance(int userId, double newBalance) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            user.setBalance(newBalance);
            return userDAO.updateUser(user);
        }
        return false;
    }

    /**
     * Deducts amount from user's balance
     * @param userId The user ID
     * @param amount The amount to deduct
     * @return true if successful, false otherwise
     */
    public boolean deductBalance(int userId, double amount) {
        double currentBalance = getUserBalance(userId);
        if (currentBalance >= amount) {
            return updateUserBalance(userId, currentBalance - amount);
        }
        return false;
    }

    /**
     * Adds amount to user's balance
     * @param userId The user ID
     * @param amount The amount to add
     * @return true if successful, false otherwise
     */
    public boolean addBalance(int userId, double amount) {
        double currentBalance = getUserBalance(userId);
        return updateUserBalance(userId, currentBalance + amount);
    }

    /**
     * Gets quantity of a specific cryptocurrency held by user
     * @param userId The user ID
     * @param symbol Cryptocurrency symbol
     * @return Quantity held
     */
    public double getHoldingQuantity(int userId, String symbol) {
        WalletItem item = walletDAO.getWalletItemByUserAndSymbol(userId, symbol);
        return item != null ? item.getQuantity() : 0;
    }
}
