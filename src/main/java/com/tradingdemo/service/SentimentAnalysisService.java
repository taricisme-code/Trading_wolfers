package com.tradingdemo.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * SentimentAnalysisService - Fetches cryptocurrency market sentiment and Fear & Greed Index
 * Provides buy/sell recommendations based on community sentiment
 */
public class SentimentAnalysisService {

    // Fear & Greed Index API (free, no key required)
    private static final String FEAR_GREED_API = "https://api.alternative.me/fng/";
    private final BinanceApiService binanceApiService = new BinanceApiService();
    
    /**
     * Fetches sentiment for a specific cryptocurrency
     * @param symbol Cryptocurrency symbol (e.g., "BTC", "ETH")
     * @return SentimentData with crypto-specific analysis
     */
    public SentimentData getCryptoSentiment(String symbol) {
        SentimentData sentiment = getFearGreedIndex();
        
        if (sentiment != null) {
            // Get crypto-specific price data
            BinanceApiService.TickerData ticker = binanceApiService.get24HourTicker(symbol);
            
            if (ticker != null) {
                // Enhance sentiment with crypto-specific data
                enhanceSentimentWithPriceData(sentiment, ticker, symbol);
            } else {
                // Fallback to generic but still mention the crypto
                sentiment.cryptoSymbol = symbol;
                addCryptoSpecificContext(sentiment, symbol);
            }
        }
        
        return sentiment;
    }
    
    /**
     * Fetches the current Fear & Greed Index (general market)
     * @return SentimentData with index value and interpretation
     */
    public SentimentData getFearGreedIndex() {
        try {
            String response = makeApiRequest(FEAR_GREED_API + "?limit=1");
            
            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                
                if (jsonObject.has("data") && jsonObject.getAsJsonArray("data").size() > 0) {
                    JsonObject data = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject();
                    
                    int value = data.get("value").getAsInt();
                    String classification = data.get("value_classification").getAsString();
                    
                    SentimentData sentiment = new SentimentData();
                    sentiment.fearGreedIndex = value;
                    sentiment.classification = classification;
                    sentiment.timestamp = data.get("timestamp").getAsLong();
                    
                    // Generate recommendation based on Fear & Greed
                    generateRecommendation(sentiment);
                    
                    return sentiment;
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching Fear & Greed Index: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Enhances sentiment with crypto-specific price data and analysis
     */
    private void enhanceSentimentWithPriceData(SentimentData sentiment, BinanceApiService.TickerData ticker, String symbol) {
        sentiment.cryptoSymbol = symbol;
        sentiment.currentPrice = ticker.currentPrice;
        sentiment.priceChange24h = ticker.priceChangePercent;
        sentiment.volume24h = ticker.quoteVolume;
        
        // Generate crypto-specific recommendation
        generateCryptoRecommendation(sentiment, ticker, symbol);
    }
    
    /**
     * Generates buy/sell recommendation based on sentiment data and crypto-specific metrics
     */
    private void generateCryptoRecommendation(SentimentData sentiment, BinanceApiService.TickerData ticker, String symbol) {
        int index = sentiment.fearGreedIndex;
        double priceChange = ticker.priceChangePercent;
        String cryptoName = getCryptoName(symbol);
        
        // Fear & Greed Index interpretation with crypto-specific context
        if (index <= 24) {
            sentiment.recommendation = "STRONG BUY";
            sentiment.signal = "BUY";
            sentiment.confidence = 90;
            
            if (priceChange < -10) {
                sentiment.reasoning = String.format("%s is down %.2f%% in 24h during Extreme Fear period. " +
                    "This double-bottom signal suggests %s is heavily oversold. Excellent accumulation opportunity. " +
                    "Historical data shows extreme fear + sharp declines often precede strong rallies.", 
                    cryptoName, Math.abs(priceChange), cryptoName);
                sentiment.confidence = 95;
            } else {
                sentiment.reasoning = String.format("Extreme Fear in the market while %s is at $%,.2f. " +
                    "Market psychology indicates panic selling. %s is likely undervalued. " +
                    "Strong buy signal for long-term positions.", 
                    cryptoName, ticker.currentPrice, cryptoName);
            }
        } else if (index <= 44) {
            sentiment.recommendation = "BUY";
            sentiment.signal = "BUY";
            sentiment.confidence = 70;
            
            if (priceChange < -5) {
                sentiment.reasoning = String.format("%s showing weakness (%.2f%% down) with market Fear at %d/100. " +
                    "Good entry point for %s. Consider dollar-cost averaging at current price $%,.2f.",
                    cryptoName, Math.abs(priceChange), index, cryptoName, ticker.currentPrice);
            } else if (priceChange > 5) {
                sentiment.reasoning = String.format("%s gaining momentum (+%.2f%%) despite market Fear. " +
                    "Strong relative strength. %s showing resilience - consider accumulating.",
                    cryptoName, priceChange, cryptoName);
                sentiment.confidence = 75;
            } else {
                sentiment.reasoning = String.format("Fear in the market with %s trading at $%,.2f. " +
                    "Good accumulation zone for %s. 24h volume: $%,.0fM indicates healthy liquidity.",
                    cryptoName, ticker.currentPrice, cryptoName, ticker.quoteVolume / 1_000_000);
            }
        } else if (index <= 55) {
            sentiment.recommendation = "HOLD";
            sentiment.signal = "NEUTRAL";
            sentiment.confidence = 50;
            
            sentiment.reasoning = String.format("%s at $%,.2f with neutral market sentiment. " +
                "24h change: %+.2f%%. Monitor %s for breakout above resistance or support breakdown. " +
                "Wait for clearer directional signals.",
                cryptoName, ticker.currentPrice, priceChange, cryptoName);
        } else if (index <= 75) {
            sentiment.recommendation = "SELL";
            sentiment.signal = "SELL";
            sentiment.confidence = 70;
            
            if (priceChange > 10) {
                sentiment.reasoning = String.format("%s surged +%.2f%% during Greed phase (index: %d). " +
                    "Parabolic move suggests %s may be overextended. Consider taking profits at $%,.2f. " +
                    "Risk/reward ratio favors sellers.",
                    cryptoName, priceChange, index, cryptoName, ticker.currentPrice);
                sentiment.confidence = 80;
            } else {
                sentiment.reasoning = String.format("Greed levels rising with %s at $%,.2f. " +
                    "%s showing signs of overvaluation. Consider reducing exposure or setting tighter stop losses. " +
                    "Protect your %s profits.",
                    cryptoName, ticker.currentPrice, cryptoName, cryptoName);
            }
        } else {
            sentiment.recommendation = "STRONG SELL";
            sentiment.signal = "SELL";
            sentiment.confidence = 90;
            
            if (priceChange > 15) {
                sentiment.reasoning = String.format("⚠️ EXTREME GREED + %s parabolic rally (+%.2f%%)! " +
                    "Historic danger zone. %s at $%,.2f is likely near local top. " +
                    "Strongly recommend securing profits NOW. High probability of sharp correction.",
                    cryptoName, priceChange, cryptoName, ticker.currentPrice);
                sentiment.confidence = 98;
            } else {
                sentiment.reasoning = String.format("Extreme Greed at %d/100 with %s trading at $%,.2f. " +
                    "Market euphoria typically precedes major corrections. %s is high-risk. " +
                    "Take profits and wait for better entry after cooldown.",
                    index, cryptoName, ticker.currentPrice, cryptoName);
            }
        }
    }
    
    /**
     * Generates buy/sell recommendation based on sentiment data (fallback without price data)
     */
    private void generateRecommendation(SentimentData sentiment) {
        int index = sentiment.fearGreedIndex;
        
        if (index <= 24) {
            sentiment.recommendation = "STRONG BUY";
            sentiment.signal = "BUY";
            sentiment.confidence = 90;
            sentiment.reasoning = "Extreme Fear indicates market oversold - excellent buying opportunity. " +
                                 "Historically, extreme fear periods have preceded major rallies.";
        } else if (index <= 44) {
            sentiment.recommendation = "BUY";
            sentiment.signal = "BUY";
            sentiment.confidence = 70;
            sentiment.reasoning = "Fear in the market suggests prices may be undervalued. " +
                                 "Good time to accumulate positions.";
        } else if (index <= 55) {
            sentiment.recommendation = "HOLD";
            sentiment.signal = "NEUTRAL";
            sentiment.confidence = 50;
            sentiment.reasoning = "Market sentiment is neutral. Wait for clearer signals before making moves. " +
                                 "Monitor for breakout or breakdown.";
        } else if (index <= 75) {
            sentiment.recommendation = "SELL";
            sentiment.signal = "SELL";
            sentiment.confidence = 70;
            sentiment.reasoning = "Greed in the market indicates potential overvaluation. " +
                                 "Consider taking profits or reducing exposure.";
        } else {
            sentiment.recommendation = "STRONG SELL";
            sentiment.signal = "SELL";
            sentiment.confidence = 90;
            sentiment.reasoning = "Extreme Greed suggests market is overbought - high risk of correction. " +
                                 "Strong sell signal - secure profits now.";
        }
    }
    
    /**
     * Adds crypto-specific context when price data unavailable
     */
    private void addCryptoSpecificContext(SentimentData sentiment, String symbol) {
        String cryptoName = getCryptoName(symbol);
        String currentReasoning = sentiment.reasoning;
        
        sentiment.reasoning = String.format("[%s Analysis] %s Focus on %s market conditions and technical levels.",
            cryptoName, currentReasoning, cryptoName);
    }
    
    /**
     * Gets full cryptocurrency name from symbol
     */
    private String getCryptoName(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC": return "Bitcoin";
            case "ETH": return "Ethereum";
            case "BNB": return "Binance Coin";
            case "ADA": return "Cardano";
            case "SOL": return "Solana";
            case "XRP": return "Ripple";
            case "DOGE": return "Dogecoin";
            case "USDC": return "USD Coin";
            default: return symbol;
        }
    }

    /**
     * Gets simplified sentiment for a specific cryptocurrency
     * Returns sentiment based on multiple factors
     */
    public String getSimplifiedSentiment(String symbol) {
        SentimentData sentiment = getFearGreedIndex();
        
        if (sentiment == null) {
            return "NEUTRAL";
        }
        
        return sentiment.signal;
    }

    /**
     * Gets detailed market analysis with recommendations
     */
    public String getDetailedAnalysis() {
        SentimentData sentiment = getFearGreedIndex();
        
        if (sentiment == null) {
            return "Unable to fetch market sentiment data.";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("═══════════════════════════════════════════════\n");
        analysis.append("        MARKET SENTIMENT ANALYSIS\n");
        analysis.append("═══════════════════════════════════════════════\n\n");
        
        analysis.append("Fear & Greed Index: ").append(sentiment.fearGreedIndex).append("/100\n");
        analysis.append("Classification: ").append(sentiment.classification.toUpperCase()).append("\n\n");
        
        analysis.append("───────────────────────────────────────────────\n");
        analysis.append("RECOMMENDATION: ").append(sentiment.recommendation).append("\n");
        analysis.append("Signal: ").append(sentiment.signal).append("\n");
        analysis.append("Confidence: ").append(sentiment.confidence).append("%\n");
        analysis.append("───────────────────────────────────────────────\n\n");
        
        analysis.append("ANALYSIS:\n");
        analysis.append(sentiment.reasoning).append("\n\n");
        
        // Add visual indicator
        analysis.append(getVisualIndicator(sentiment.fearGreedIndex)).append("\n");
        
        return analysis.toString();
    }

    /**
     * Creates a visual indicator bar for the Fear & Greed Index
     */
    private String getVisualIndicator(int index) {
        StringBuilder bar = new StringBuilder("\n[");
        
        for (int i = 0; i <= 100; i += 5) {
            if (i <= index) {
                if (i <= 24) bar.append("█"); // Extreme Fear (green)
                else if (i <= 44) bar.append("▓"); // Fear (light green)
                else if (i <= 55) bar.append("▒"); // Neutral (gray)
                else if (i <= 75) bar.append("▓"); // Greed (orange)
                else bar.append("█"); // Extreme Greed (red)
            } else {
                bar.append("░");
            }
        }
        
        bar.append("]\n");
        bar.append(" Fear                  Neutral                 Greed");
        
        return bar.toString();
    }

    /**
     * Makes an HTTP GET request
     */
    private String makeApiRequest(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
        } catch (Exception e) {
            System.err.println("Error making sentiment API request: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Tests API connectivity
     */
    public boolean testConnection() {
        try {
            SentimentData data = getFearGreedIndex();
            return data != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Data class for sentiment information
     */
    public static class SentimentData {
        public int fearGreedIndex;
        public String classification;
        public long timestamp;
        public String recommendation;
        public String signal; // BUY, SELL, NEUTRAL
        public int confidence; // 0-100
        public String reasoning;
        public String cryptoSymbol; // Specific crypto being analyzed
        public double currentPrice; // Current price of the crypto
        public double priceChange24h; // 24h price change percentage
        public double volume24h; // 24h trading volume

        @Override
        public String toString() {
            if (cryptoSymbol != null) {
                return String.format("%s - %s (%d/100) - %s [%d%% confidence]", 
                    cryptoSymbol, classification, fearGreedIndex, recommendation, confidence);
            }
            return String.format("%s (%d/100) - %s [%d%% confidence]", 
                classification, fearGreedIndex, recommendation, confidence);
        }
    }
}
