package com.tradingdemo.service;

import java.util.List;

import com.tradingdemo.dao.NewsDAO;
import com.tradingdemo.model.News;

/**
 * NewsService - Business logic for cryptocurrency news
 * Handles news retrieval and management
 */
public class NewsService {

    private final NewsDAO newsDAO;

    public NewsService() {
        this.newsDAO = new NewsDAO();
    }

    /**
     * Gets all news
     * @return List of all news
     */
    public List<News> getAllNews() {
        return newsDAO.getAllNews();
    }

    /**
     * Gets news by source
     * @param source The news source
     * @return List of news from that source
     */
    public List<News> getNewsBySource(String source) {
        return newsDAO.getNewsBySource(source);
    }

    /**
     * Creates a new news article
     * @param title News title
     * @param content News content
     * @param source News source
     * @return The created News object or null if failed
     */
    public News createNews(String title, String content, String source) {
        News news = new News(title, content, source);
        if (newsDAO.createNews(news)) {
            return news;
        }
        return null;
    }

    /**
     * Gets a specific news article
     * @param newsId The news ID
     * @return News object or null if not found
     */
    public News getNewsById(int newsId) {
        return newsDAO.getNewsById(newsId);
    }

    /**
     * Deletes a news article
     * @param newsId The news ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteNews(int newsId) {
        return newsDAO.deleteNews(newsId);
    }

    /**
     * Initializes demo news articles
     * Should be called on app startup
     */
    public void initializeDemoNews() {
        // Check if news already exists
        if (newsDAO.getAllNews().isEmpty()) {
            createNews(
                "Bitcoin Reaches New All-Time High",
                "Bitcoin has surpassed previous records as institutional adoption increases worldwide. Analysts suggest continued bullish momentum.",
                "CryptoNews Daily"
            );

            createNews(
                "Ethereum 2.0 Updates Show Promise",
                "Latest Ethereum network upgrades demonstrate significant improvements in scalability and energy efficiency.",
                "Blockchain Times"
            );

            createNews(
                "SEC Approves First Spot Bitcoin ETF",
                "In a landmark decision, the SEC has approved the first spot Bitcoin ETF, making crypto more accessible to retail investors.",
                "Finance Weekly"
            );

            createNews(
                "DeFi Platform Reaches $100B TVL",
                "Major decentralized finance protocol announces reaching $100 billion in total value locked, marking significant growth.",
                "DeFi Pulse"
            );

            createNews(
                "Cryptocurrency Market Sees Strong Recovery",
                "Following recent market volatility, cryptocurrencies show signs of recovery with major coins gaining momentum.",
                "Market Watch"
            );
        }
    }
}
