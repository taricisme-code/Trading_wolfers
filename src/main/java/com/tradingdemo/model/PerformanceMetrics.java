package com.tradingdemo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PerformanceMetrics {
    public int totalTrades = 0;
    public int totalBuys = 0;
    public int totalSells = 0;
    public int closedTrades = 0; // sell trades matched
    public int winningTrades = 0;
    public int losingTrades = 0;
    public double realizedPnl = 0.0;
    public double avgProfitPerTrade = 0.0;
    public double winRate = 0.0;

    // Simple equity curve: pairs of timestamp + cumulative PnL
    public List<LocalDateTime> equityTimestamps = new ArrayList<>();
    public List<Double> equityValues = new ArrayList<>();
}
