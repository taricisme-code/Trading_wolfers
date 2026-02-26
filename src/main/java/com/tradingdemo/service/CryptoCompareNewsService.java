package com.tradingdemo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tradingdemo.model.News;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * CryptoCompareNewsService - Fetches real-time cryptocurrency news from CryptoCompare API
 * Provides latest crypto market news with auto-refresh capability
 */
public class CryptoCompareNewsService {

    // CryptoCompare API endpoint - No API key required for basic usage
    private static final String API_BASE = "https://min-api.cryptocompare.com/data/v2/news/";
    private static final String API_KEY = ""; // Optional: Add your API key for higher rate limits
    
    /**
     * Fetches latest cryptocurrency news
     * @param limit Number of news articles to fetch (default: 50, max: 100)
     * @return List of News objects
     */
    public List<News> getLatestNews(int limit) {
        List<News> newsList = new ArrayList<>();
        
        try {
            String urlString = API_BASE + "?lang=EN";
            if (limit > 0) {
                urlString += "&limit=" + Math.min(limit, 100);
            }
            if (!API_KEY.isEmpty()) {
                urlString += "&api_key=" + API_KEY;
            }
            
            String response = makeApiRequest(urlString);
            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                
                // Check if the response is successful
                String responseStatus = jsonObject.get("Response").getAsString();
                if (!"Success".equals(responseStatus)) {
                    System.err.println("CryptoCompare API error: " + jsonObject.get("Message").getAsString());
                    return newsList;
                }
                
                JsonArray dataArray = jsonObject.getAsJsonObject("Data").getAsJsonArray();
                
                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject article = dataArray.get(i).getAsJsonObject();
                    
                    News news = new News();
                    news.setTitle(article.get("title").getAsString());
                    news.setContent(article.get("body").getAsString());
                    news.setSource(article.getAsJsonObject("source_info").get("name").getAsString());
                    
                    // Convert timestamp to LocalDateTime
                    long timestamp = article.get("published_on").getAsLong();
                    LocalDateTime publishedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
                    news.setPublishedAt(publishedAt);
                    
                    // Additional fields from CryptoCompare
                    if (article.has("imageurl") && !article.get("imageurl").isJsonNull()) {
                        news.setImageUrl(article.get("imageurl").getAsString());
                    }
                    if (article.has("url") && !article.get("url").isJsonNull()) {
                        news.setUrl(article.get("url").getAsString());
                    }
                    if (article.has("tags") && !article.get("tags").isJsonNull()) {
                        news.setTags(article.get("tags").getAsString());
                    }
                    if (article.has("categories") && !article.get("categories").isJsonNull()) {
                        news.setCategories(article.get("categories").getAsString());
                    }
                    
                    newsList.add(news);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching crypto news: " + e.getMessage());
            e.printStackTrace();
        }
        
        return newsList;
    }

    /**
     * Fetches news filtered by specific cryptocurrency
     * @param cryptoSymbol Cryptocurrency symbol (e.g., "BTC", "ETH")
     * @param limit Number of news articles to fetch
     * @return List of News objects related to the specified crypto
     */
    public List<News> getNewsByCategory(String cryptoSymbol, int limit) {
        List<News> allNews = getLatestNews(limit * 2); // Fetch more to filter
        List<News> filteredNews = new ArrayList<>();
        
        String searchTerm = cryptoSymbol.toUpperCase();
        
        for (News news : allNews) {
            String tags = news.getTags() != null ? news.getTags().toUpperCase() : "";
            String categories = news.getCategories() != null ? news.getCategories().toUpperCase() : "";
            String title = news.getTitle().toUpperCase();
            String content = news.getContent().toUpperCase();
            
            if (tags.contains(searchTerm) || categories.contains(searchTerm) || 
                title.contains(searchTerm) || content.contains(searchTerm)) {
                filteredNews.add(news);
                if (filteredNews.size() >= limit) {
                    break;
                }
            }
        }
        
        return filteredNews;
    }

    /**
     * Asynchronously fetches latest news
     * @param limit Number of news articles
     * @return CompletableFuture with List of News
     */
    public CompletableFuture<List<News>> getLatestNewsAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> getLatestNews(limit));
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
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

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
                System.err.println("CryptoCompare API request failed with code: " + responseCode);
                
                // Try to read error response
                if (connection.getErrorStream() != null) {
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()));
                    String errorLine;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    System.err.println("Error response: " + errorResponse.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error making CryptoCompare API request: " + e.getMessage());
            e.printStackTrace();
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
            List<News> testNews = getLatestNews(1);
            return testNews != null && !testNews.isEmpty();
        } catch (Exception e) {
            System.err.println("CryptoCompare API connection test failed: " + e.getMessage());
            return false;
        }
    }
}
