package com.tradingdemo.service;

import java.util.List;
import java.util.Random;

import com.tradingdemo.dao.PredictionDAO;
import com.tradingdemo.model.Prediction;

/**
 * PredictionService - Business logic for AI-based price predictions
 * Handles prediction generation and retrieval (simulated for demo)
 */
public class PredictionService {

    private final PredictionDAO predictionDAO;
    private final Random random = new Random();
    private final String[] SIGNALS = {"BUY", "SELL", "HOLD"};

    public PredictionService() {
        this.predictionDAO = new PredictionDAO();
    }

    /**
     * Generates a simulated prediction for a cryptocurrency
     * @param symbol The cryptocurrency symbol
     * @param currentPrice Current market price
     * @return The generated Prediction object
     */
    public Prediction generatePrediction(String symbol, double currentPrice) {
        String signal = SIGNALS[random.nextInt(SIGNALS.length)];
        double confidence = 0.6 + (random.nextDouble() * 0.35); // 60-95% confidence
        double variance = currentPrice * 0.1; // 10% variance
        double targetPrice = currentPrice + ((random.nextDouble() - 0.5) * 2 * variance);

        String analysis = generateAnalysis(symbol, signal, confidence, currentPrice, targetPrice);
        
        Prediction prediction = new Prediction(symbol, signal, confidence, targetPrice, analysis);
        predictionDAO.createPrediction(prediction);
        
        return prediction;
    }

    /**
     * Gets the latest prediction for a cryptocurrency
     * @param symbol The cryptocurrency symbol
     * @return The latest Prediction or null if none exists
     */
    public Prediction getLatestPrediction(String symbol) {
        return predictionDAO.getLatestPredictionBySymbol(symbol);
    }

    /**
     * Gets all predictions for a symbol
     * @param symbol The cryptocurrency symbol
     * @return List of predictions
     */
    public List<Prediction> getPredictionsBySymbol(String symbol) {
        return predictionDAO.getPredictionsBySymbol(symbol);
    }

    /**
     * Gets all available predictions
     * @return List of all predictions
     */
    public List<Prediction> getAllPredictions() {
        return predictionDAO.getAllPredictions();
    }

    /**
     * Generates AI analysis text (simulated)
     * @param symbol Cryptocurrency symbol
     * @param signal Trading signal
     * @param confidence Confidence level
     * @param currentPrice Current price
     * @param targetPrice Target price
     * @return Analysis text
     */
    private String generateAnalysis(String symbol, String signal, double confidence, 
                                    double currentPrice, double targetPrice) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("AI Analysis for ").append(symbol).append(":\n");
        analysis.append("Current Price: $").append(String.format("%.2f", currentPrice)).append("\n");
        analysis.append("Target Price: $").append(String.format("%.2f", targetPrice)).append("\n");
        analysis.append("Confidence: ").append(String.format("%.1f%%", confidence * 100)).append("\n");
        
        switch (signal) {
            case "BUY" -> analysis.append("Signal: Strong upward trend detected. Market momentum favors buying.");
            case "SELL" -> analysis.append("Signal: Downward pressure observed. Consider reducing positions.");
            case "HOLD" -> analysis.append("Signal: Market consolidation phase. Recommend holding current positions.");
        }
        
        return analysis.toString();
    }

    /**
     * Simulates ML model accuracy rating
     * @return Accuracy between 0-100
     */
    public double getModelAccuracy() {
        // Simulated accuracy: 65-85%
        return 65 + (random.nextDouble() * 20);
    }
}
