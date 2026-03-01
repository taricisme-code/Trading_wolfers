package com.tradingdemo.model;

import java.time.LocalDateTime;

public class AlertRule {
    private int id;
    private int userId;
    private String symbol;
    private double targetPrice;
    private boolean notifyWhenAbove; // true = notify when price >= target, false = when <= target
    private boolean enabled = true;
    private boolean notifyEmail = true;
    private boolean notifySms = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public AlertRule() {}

    public AlertRule(int userId, String symbol, double targetPrice, boolean notifyWhenAbove) {
        this.userId = userId;
        this.symbol = symbol;
        this.targetPrice = targetPrice;
        this.notifyWhenAbove = notifyWhenAbove;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }
    public boolean isNotifyWhenAbove() { return notifyWhenAbove; }
    public void setNotifyWhenAbove(boolean notifyWhenAbove) { this.notifyWhenAbove = notifyWhenAbove; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(boolean notifyEmail) { this.notifyEmail = notifyEmail; }
    public boolean isNotifySms() { return notifySms; }
    public void setNotifySms(boolean notifySms) { this.notifySms = notifySms; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
