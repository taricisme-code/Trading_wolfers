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

/**
 * CryptoPanicNewsService - Fetches cryptocurrency news from CryptoPanic API
 * CryptoPanic aggregates news from multiple crypto sources
 */
public class CryptoPanicNewsService {

    // CryptoPanic API - Free tier available (no auth required for public endpoint)
    private static final String API_BASE = "https://cryptopanic.com/api/v1/posts/";
    private static final String API_KEY = ""; // Optional: Add your free API key for higher limits
    
    /**
     * Fetches latest cryptocurrency news from CryptoPanic
     * @param limit Number of news articles to fetch
     * @return List of News objects
     */
    public List<News> getLatestNews(int limit) {
        List<News> newsList = new ArrayList<>();
        
        try {
            String urlString = API_BASE + "?kind=news&public=true";
            if (!API_KEY.isEmpty()) {
                urlString += "&auth_token=" + API_KEY;
            }
            
            String response = makeApiRequest(urlString);
            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                JsonArray resultsArray = jsonObject.getAsJsonArray("results");
                
                int count = 0;
                for (int i = 0; i < resultsArray.size() && count < limit; i++) {
                    JsonObject article = resultsArray.get(i).getAsJsonObject();
                    
                    News news = new News();
                    news.setTitle(article.get("title").getAsString());
                    
                    // CryptoPanic doesn't provide full content, use title as preview
                    String url = article.get("url").getAsString();
                    news.setContent("Read full article at: " + url);
                    news.setUrl(url);
                    
                    // Get source
                    if (article.has("source") && !article.get("source").isJsonNull()) {
                        JsonObject source = article.getAsJsonObject("source");
                        news.setSource(source.get("title").getAsString());
                    } else {
                        news.setSource("CryptoPanic");
                    }
                    
                    // Convert timestamp
                    String createdAt = article.get("created_at").getAsString();
                    LocalDateTime publishedAt = LocalDateTime.parse(createdAt.substring(0, 19));
                    news.setPublishedAt(publishedAt);
                    
                    // Get currencies (tags)
                    if (article.has("currencies") && !article.get("currencies").isJsonNull()) {
                        JsonArray currencies = article.getAsJsonArray("currencies");
                        StringBuilder tags = new StringBuilder();
                        for (int j = 0; j < currencies.size(); j++) {
                            JsonObject currency = currencies.get(j).getAsJsonObject();
                            if (j > 0) tags.append(", ");
                            tags.append(currency.get("code").getAsString());
                        }
                        news.setTags(tags.toString());
                    }
                    
                    newsList.add(news);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching CryptoPanic news: " + e.getMessage());
            e.printStackTrace();
        }
        
        return newsList;
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
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

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
            } else {
                System.err.println("CryptoPanic API request failed with code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error making CryptoPanic API request: " + e.getMessage());
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
            List<News> testNews = getLatestNews(1);
            return testNews != null && !testNews.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
