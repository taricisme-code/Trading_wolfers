package com.tradingdemo.model;

import java.time.LocalDateTime;

/**
 * Trade entity representing an executed trade
 */
public class Trade {
    private int id;
    private int orderId;
    private String symbol;
    private String side;
    private double executedPrice;
    private double quantity;
    private LocalDateTime executedAt;

    // Constructors
    public Trade() {
    }

    public Trade(int orderId, String symbol, String side, double executedPrice, double quantity) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.executedPrice = executedPrice;
        this.quantity = quantity;
        this.executedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public double getExecutedPrice() {
        return executedPrice;
    }

    public void setExecutedPrice(double executedPrice) {
        this.executedPrice = executedPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", executedPrice=" + executedPrice +
                ", quantity=" + quantity +
                '}';
    }
}
