package com.tradingdemo.model;

/**
 * Prediction entity representing AI price predictions
 */
public class Prediction {
    private int id;
    private String symbol;
    private String signal;          // BUY, SELL, HOLD
    private double confidence;      // 0.0 to 1.0
    private double targetPrice;
    private String analysis;

    // Constructors
    public Prediction() {
    }

    public Prediction(String symbol, String signal, double confidence, double targetPrice, String analysis) {
        this.symbol = symbol;
        this.signal = signal;
        this.confidence = confidence;
        this.targetPrice = targetPrice;
        this.analysis = analysis;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "symbol='" + symbol + '\'' +
                ", signal='" + signal + '\'' +
                ", confidence=" + confidence +
                ", targetPrice=" + targetPrice +
                '}';
    }
}
