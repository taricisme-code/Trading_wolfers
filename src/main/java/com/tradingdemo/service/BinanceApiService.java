package com.tradingdemo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * BinanceApiService - Handles real-time cryptocurrency data from Binance Public API
 * Provides live price updates, 24h volume, and market statistics
 */
public class BinanceApiService {

    private static final String BINANCE_API_BASE = "https://api.binance.com/api/v3";
    private static final String TICKER_24HR_ENDPOINT = "/ticker/24hr";
    private static final String TICKER_PRICE_ENDPOINT = "/ticker/price";
    
    // Map crypto symbols to Binance trading pairs
    private static final Map<String, String> SYMBOL_MAPPING = new HashMap<>();
    
    static {
        SYMBOL_MAPPING.put("BTC", "BTCUSDT");
        SYMBOL_MAPPING.put("ETH", "ETHUSDT");
        SYMBOL_MAPPING.put("BNB", "BNBUSDT");
        SYMBOL_MAPPING.put("ADA", "ADAUSDT");
        SYMBOL_MAPPING.put("SOL", "SOLUSDT");
        SYMBOL_MAPPING.put("XRP", "XRPUSDT");
        SYMBOL_MAPPING.put("DOGE", "DOGEUSDT");
        SYMBOL_MAPPING.put("USDC", "USDCUSDT");
    }

    /**
     * Fetches real-time price for a cryptocurrency
     * @param symbol Cryptocurrency symbol (e.g., "BTC")
     * @return Current price or null if failed
     */
    public Double getCurrentPrice(String symbol) {
        try {
            String binanceSymbol = SYMBOL_MAPPING.getOrDefault(symbol, symbol + "USDT");
            String urlString = BINANCE_API_BASE + TICKER_PRICE_ENDPOINT + "?symbol=" + binanceSymbol;
            
            String response = makeApiRequest(urlString);
            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                return jsonObject.get("price").getAsDouble();
            }
        } catch (Exception e) {
            System.err.println("Error fetching price for " + symbol + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches comprehensive 24-hour ticker data including price, volume, and market cap indicators
     * @param symbol Cryptocurrency symbol (e.g., "BTC")
     * @return TickerData object with complete market statistics or null if failed
     */
    public TickerData get24HourTicker(String symbol) {
        try {
            String binanceSymbol = SYMBOL_MAPPING.getOrDefault(symbol, symbol + "USDT");
            String urlString = BINANCE_API_BASE + TICKER_24HR_ENDPOINT + "?symbol=" + binanceSymbol;
            
            String response = makeApiRequest(urlString);
            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                
                TickerData data = new TickerData();
                data.symbol = symbol;
                data.currentPrice = jsonObject.get("lastPrice").getAsDouble();
                data.priceChange = jsonObject.get("priceChange").getAsDouble();
                data.priceChangePercent = jsonObject.get("priceChangePercent").getAsDouble();
                data.highPrice = jsonObject.get("highPrice").getAsDouble();
                data.lowPrice = jsonObject.get("lowPrice").getAsDouble();
                data.volume = jsonObject.get("volume").getAsDouble();
                data.quoteVolume = jsonObject.get("quoteVolume").getAsDouble();
                data.openPrice = jsonObject.get("openPrice").getAsDouble();
                data.prevClosePrice = jsonObject.get("prevClosePrice").getAsDouble();
                
                return data;
            }
        } catch (Exception e) {
            System.err.println("Error fetching 24h ticker for " + symbol + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches multiple cryptocurrency prices in a single request (more efficient)
     * @return Map of symbol to price
     */
    public Map<String, Double> getAllPrices() {
        Map<String, Double> prices = new HashMap<>();
        try {
            String urlString = BINANCE_API_BASE + TICKER_PRICE_ENDPOINT;
            String response = makeApiRequest(urlString);
            
            if (response != null) {
                JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject ticker = jsonArray.get(i).getAsJsonObject();
                    String symbol = ticker.get("symbol").getAsString();
                    double price = ticker.get("price").getAsDouble();
                    
                    // Map back to our symbols
                    for (Map.Entry<String, String> entry : SYMBOL_MAPPING.entrySet()) {
                        if (entry.getValue().equals(symbol)) {
                            prices.put(entry.getKey(), price);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching all prices: " + e.getMessage());
        }
        return prices;
    }

    /**
     * Asynchronously fetches 24-hour ticker data
     * @param symbol Cryptocurrency symbol
     * @return CompletableFuture with TickerData
     */
    public CompletableFuture<TickerData> get24HourTickerAsync(String symbol) {
        return CompletableFuture.supplyAsync(() -> get24HourTicker(symbol));
    }

    /**
     * Makes an HTTP GET request to the specified URL
     * @param urlString The API endpoint URL
     * @return Response body as String or null if failed
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
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                System.err.println("API request failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error making API request: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Tests API connectivity
     * @return true if API is accessible, false otherwise
     */
    public boolean testConnection() {
        try {
            String response = makeApiRequest(BINANCE_API_BASE + "/ping");
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Data class to hold 24-hour ticker information
     */
    public static class TickerData {
        public String symbol;
        public double currentPrice;
        public double priceChange;
        public double priceChangePercent;
        public double highPrice;
        public double lowPrice;
        public double volume;
        public double quoteVolume; // Volume in quote asset (USD)
        public double openPrice;
        public double prevClosePrice;

        @Override
        public String toString() {
            return String.format("%s: $%.2f (%.2f%%)", symbol, currentPrice, priceChangePercent);
        }
    }
}
