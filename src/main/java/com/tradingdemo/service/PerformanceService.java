package com.tradingdemo.service;

import com.tradingdemo.dao.TradeDAO;
import com.tradingdemo.model.PerformanceMetrics;
import com.tradingdemo.model.Trade;

import java.time.LocalDateTime;
import java.util.*;

public class PerformanceService {

    private final TradeDAO tradeDAO = new TradeDAO();

    public PerformanceMetrics computeForUser(int userId) {
        PerformanceMetrics m = new PerformanceMetrics();

        List<Trade> trades = tradeDAO.getTradesByUserId(userId);
        // sort ascending by executedAt
        trades.sort(Comparator.comparing(Trade::getExecutedAt));

        m.totalTrades = trades.size();

        // FIFO buy lots per symbol
        Map<String, Deque<Lot>> buyLots = new HashMap<>();
        double cumulative = 0.0;

        for (Trade t : trades) {
            if ("BUY".equalsIgnoreCase(t.getSide())) {
                m.totalBuys++;
                buyLots.computeIfAbsent(t.getSymbol(), s -> new ArrayDeque<>())
                        .addLast(new Lot(t.getQuantity(), t.getExecutedPrice()));
            } else if ("SELL".equalsIgnoreCase(t.getSide())) {
                m.totalSells++;
                double remaining = t.getQuantity();
                double tradeProfit = 0.0;
                Deque<Lot> deque = buyLots.getOrDefault(t.getSymbol(), new ArrayDeque<>());
                while (remaining > 1e-9 && !deque.isEmpty()) {
                    Lot lot = deque.peekFirst();
                    double used = Math.min(remaining, lot.quantity);
                    double profit = (t.getExecutedPrice() - lot.price) * used;
                    tradeProfit += profit;
                    lot.quantity -= used;
                    remaining -= used;
                    if (lot.quantity <= 1e-9) deque.pollFirst();
                }
                // If there were no buys to match, assume entire sell is closed with cost 0 (conservative)
                if (remaining > 1e-9) {
                    // treat remaining as sold without buy -> count as realized at full price
                    tradeProfit += t.getExecutedPrice() * remaining;
                    remaining = 0;
                }

                m.realizedPnl += tradeProfit;
                m.closedTrades++;
                if (tradeProfit > 0) m.winningTrades++; else if (tradeProfit < 0) m.losingTrades++;

                cumulative += tradeProfit;
                m.equityTimestamps.add(t.getExecutedAt());
                m.equityValues.add(cumulative);
            }
        }

        if (m.closedTrades > 0) {
            m.avgProfitPerTrade = m.realizedPnl / m.closedTrades;
            m.winRate = (double) m.winningTrades / (double) m.closedTrades;
        }

        return m;
    }

    private static class Lot {
        double quantity;
        double price;

        Lot(double quantity, double price) {
            this.quantity = quantity;
            this.price = price;
        }
    }
}
