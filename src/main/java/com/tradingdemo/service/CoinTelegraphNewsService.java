package com.tradingdemo.service;

import com.tradingdemo.model.News;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CoinTelegraphNewsService - Fetches cryptocurrency news from Cointelegraph RSS feed
 * Cointelegraph is a major crypto news publisher
 */
public class CoinTelegraphNewsService {

    private static final String RSS_FEED_URL = "https://cointelegraph.com/rss";
    
    /**
     * Fetches latest news from Cointelegraph RSS feed
     * @param limit Number of articles to fetch
     * @return List of News objects
     */
    public List<News> getLatestNews(int limit) {
        List<News> newsList = new ArrayList<>();
        
        try {
            String rssContent = makeRequest(RSS_FEED_URL);
            if (rssContent != null && !rssContent.isEmpty()) {
                newsList = parseRssFeed(rssContent, limit);
            }
        } catch (Exception e) {
            System.err.println("Error fetching Cointelegraph news: " + e.getMessage());
        }
        
        return newsList;
    }
    
    /**
     * Parses RSS feed content
     */
    private List<News> parseRssFeed(String rssContent, int limit) {
        List<News> newsList = new ArrayList<>();
        
        try {
            // Extract items from RSS
            Pattern itemPattern = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
            Matcher itemMatcher = itemPattern.matcher(rssContent);
            
            int count = 0;
            while (itemMatcher.find() && count < limit) {
                String itemContent = itemMatcher.group(1);
                
                News news = new News();
                news.setSource("Cointelegraph");
                
                // Extract title
                String title = extractTag(itemContent, "title");
                if (title != null) {
                    news.setTitle(cleanCData(title));
                }
                
                // Extract link
                String link = extractTag(itemContent, "link");
                if (link != null) {
                    news.setUrl(link.trim());
                }
                
                // Extract description
                String description = extractTag(itemContent, "description");
                if (description != null) {
                    news.setContent(cleanHtml(cleanCData(description)));
                }
                
                // Extract publication date
                String pubDate = extractTag(itemContent, "pubDate");
                if (pubDate != null) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
                        news.setPublishedAt(LocalDateTime.parse(pubDate.trim(), formatter));
                    } catch (Exception e) {
                        news.setPublishedAt(LocalDateTime.now());
                    }
                }
                
                // Extract categories
                Pattern categoryPattern = Pattern.compile("<category>(.*?)</category>");
                Matcher categoryMatcher = categoryPattern.matcher(itemContent);
                StringBuilder tags = new StringBuilder();
                while (categoryMatcher.find()) {
                    if (tags.length() > 0) tags.append(", ");
                    tags.append(cleanCData(categoryMatcher.group(1)));
                }
                if (tags.length() > 0) {
                    news.setTags(tags.toString());
                }
                
                if (news.getTitle() != null && !news.getTitle().isEmpty()) {
                    newsList.add(news);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Cointelegraph RSS: " + e.getMessage());
        }
        
        return newsList;
    }
    
    /**
     * Extracts content from XML tag
     */
    private String extractTag(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Removes CDATA wrapper
     */
    private String cleanCData(String text) {
        if (text == null) return "";
        return text.replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "").trim();
    }
    
    /**
     * Removes HTML tags
     */
    private String cleanHtml(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ")
                   .replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">").replaceAll("&quot;", "\"").trim();
    }
    
    /**
     * Makes HTTP GET request
     */
    private String makeRequest(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                return response.toString();
            } else {
                System.err.println("Cointelegraph RSS returned code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error making request to Cointelegraph: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
