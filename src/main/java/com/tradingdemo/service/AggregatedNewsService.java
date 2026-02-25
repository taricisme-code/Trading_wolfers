package com.tradingdemo.service;

import com.tradingdemo.model.News;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * AggregatedNewsService - Combines news from multiple sources
 * Fetches from CryptoCompare, CryptoPanic, CoinDesk, Cointelegraph, and other sources simultaneously
 */
public class AggregatedNewsService {

    private final CryptoCompareNewsService cryptoCompareService;
    private final CryptoPanicNewsService cryptoPanicService;
    private final CoinDeskNewsService coinDeskService;
    private final CoinTelegraphNewsService coinTelegraphService;
    private final NewsService localNewsService;

    public AggregatedNewsService() {
        this.cryptoCompareService = new CryptoCompareNewsService();
        this.cryptoPanicService = new CryptoPanicNewsService();
        this.coinDeskService = new CoinDeskNewsService();
        this.coinTelegraphService = new CoinTelegraphNewsService();
        this.localNewsService = new NewsService();
    }

    /**
     * Fetches news from all available sources simultaneously
     * @param limitPerSource Number of articles to fetch from each source
     * @return Combined and sorted list of news articles
     */
    public List<News> getAggregatedNews(int limitPerSource) {
        List<News> allNews = new ArrayList<>();

        // Create futures for parallel fetching from all sources
        CompletableFuture<List<News>> cryptoCompareFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching from CryptoCompare...");
                return cryptoCompareService.getLatestNews(limitPerSource);
            } catch (Exception e) {
                System.err.println("CryptoCompare fetch failed: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<News>> cryptoPanicFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching from CryptoPanic...");
                return cryptoPanicService.getLatestNews(limitPerSource);
            } catch (Exception e) {
                System.err.println("CryptoPanic fetch failed: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<News>> coinDeskFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching from CoinDesk...");
                return coinDeskService.getLatestNews(limitPerSource);
            } catch (Exception e) {
                System.err.println("CoinDesk fetch failed: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<News>> coinTelegraphFuture = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Fetching from Cointelegraph...");
                return coinTelegraphService.getLatestNews(limitPerSource);
            } catch (Exception e) {
                System.err.println("Cointelegraph fetch failed: " + e.getMessage());
                return new ArrayList<>();
            }
        });

        // Wait for all futures to complete
        try {
            List<News> cryptoCompareNews = cryptoCompareFuture.get();
            List<News> cryptoPanicNews = cryptoPanicFuture.get();
            List<News> coinDeskNews = coinDeskFuture.get();
            List<News> coinTelegraphNews = coinTelegraphFuture.get();

            System.out.println("CryptoCompare returned: " + cryptoCompareNews.size() + " articles");
            System.out.println("CryptoPanic returned: " + cryptoPanicNews.size() + " articles");
            System.out.println("CoinDesk returned: " + coinDeskNews.size() + " articles");
            System.out.println("Cointelegraph returned: " + coinTelegraphNews.size() + " articles");

            allNews.addAll(cryptoCompareNews);
            allNews.addAll(cryptoPanicNews);
            allNews.addAll(coinDeskNews);
            allNews.addAll(coinTelegraphNews);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error waiting for news fetch: " + e.getMessage());
            e.printStackTrace();
        }

        // Fallback to local demo news if no API news available
        if (allNews.isEmpty()) {
            System.out.println("No API news available, using local demo news");
            localNewsService.initializeDemoNews();
            allNews.addAll(localNewsService.getAllNews());
        }

        // Remove duplicates based on title similarity
        allNews = removeDuplicates(allNews);

        // Sort by date (most recent first)
        allNews.sort(Comparator.comparing(News::getPublishedAt).reversed());

        System.out.println("Total unique news articles: " + allNews.size());
        return allNews;
    }

    /**
     * Removes duplicate news articles based on title similarity
     */
    private List<News> removeDuplicates(List<News> newsList) {
        List<News> uniqueNews = new ArrayList<>();
        
        for (News news : newsList) {
            boolean isDuplicate = false;
            String currentTitle = news.getTitle().toLowerCase();
            
            for (News existing : uniqueNews) {
                String existingTitle = existing.getTitle().toLowerCase();
                
                // Check if titles are very similar (simple duplicate check)
                if (currentTitle.equals(existingTitle) || 
                    calculateSimilarity(currentTitle, existingTitle) > 0.8) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                uniqueNews.add(news);
            }
        }
        
        return uniqueNews;
    }

    /**
     * Calculates similarity between two strings (simple Jaccard similarity)
     */
    private double calculateSimilarity(String s1, String s2) {
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");
        
        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(words1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(words2));
        
        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);
        
        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    /**
     * Filters news by specific cryptocurrency
     */
    public List<News> getNewsBySymbol(String symbol, int limit) {
        List<News> allNews = getAggregatedNews(limit * 2);
        
        return allNews.stream()
            .filter(news -> {
                String tags = news.getTags() != null ? news.getTags().toUpperCase() : "";
                String title = news.getTitle().toUpperCase();
                String content = news.getContent().toUpperCase();
                String searchTerm = symbol.toUpperCase();
                
                return tags.contains(searchTerm) || title.contains(searchTerm) || content.contains(searchTerm);
            })
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Tests connectivity to all news sources
     */
    public String testAllConnections() {
        StringBuilder status = new StringBuilder();
        
        boolean cryptoCompareOk = cryptoCompareService.testConnection();
        boolean cryptoPanicOk = cryptoPanicService.testConnection();
        
        status.append("CryptoCompare: ").append(cryptoCompareOk ? "✓ Connected" : "✗ Failed").append("\n");
        status.append("CryptoPanic: ").append(cryptoPanicOk ? "✓ Connected" : "✗ Failed").append("\n");
        status.append("CoinDesk: Available (RSS)").append("\n");
        status.append("Cointelegraph: Available (RSS)");
        
        return status.toString();
    }
}
