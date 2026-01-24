package com.tradingdemo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.tradingdemo.config.DatabaseConnection;
import com.tradingdemo.model.News;

/**
 * NewsDAO - Data Access Object for News entity
 * Handles all database operations for cryptocurrency news
 */
public class NewsDAO {

    private final Connection connection;

    public NewsDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createNews(News news) {
        String sql = "INSERT INTO news (title, content, source, published_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getContent());
            stmt.setString(3, news.getSource());
            stmt.setTimestamp(4, Timestamp.valueOf(news.getPublishedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        news.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating news: " + e.getMessage());
        }
        return false;
    }

    public News getNewsById(int newsId) {
        String sql = "SELECT * FROM news WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newsId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToNews(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving news: " + e.getMessage());
        }
        return null;
    }

    public List<News> getAllNews() {
        List<News> newsList = new ArrayList<>();
        String sql = "SELECT * FROM news ORDER BY published_at DESC LIMIT 50";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                newsList.add(mapResultSetToNews(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all news: " + e.getMessage());
        }
        return newsList;
    }

    public List<News> getNewsBySource(String source) {
        List<News> newsList = new ArrayList<>();
        String sql = "SELECT * FROM news WHERE source = ? ORDER BY published_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, source);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                newsList.add(mapResultSetToNews(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving news by source: " + e.getMessage());
        }
        return newsList;
    }

    public boolean deleteNews(int newsId) {
        String sql = "DELETE FROM news WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newsId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting news: " + e.getMessage());
        }
        return false;
    }

    private News mapResultSetToNews(ResultSet rs) throws SQLException {
        News news = new News();
        news.setId(rs.getInt("id"));
        news.setTitle(rs.getString("title"));
        news.setContent(rs.getString("content"));
        news.setSource(rs.getString("source"));
        
        Timestamp publishedTs = rs.getTimestamp("published_at");
        if (publishedTs != null) {
            news.setPublishedAt(publishedTs.toLocalDateTime());
        }
        
        return news;
    }
}
