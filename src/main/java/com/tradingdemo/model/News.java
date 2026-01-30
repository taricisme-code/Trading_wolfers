package com.tradingdemo.model;

import java.time.LocalDateTime;

/**
 * News entity representing cryptocurrency news
 */
public class News {
    private int id;
    private String title;
    private String content;
    private String source;
    private LocalDateTime publishedAt;

    // Constructors
    public News() {
    }

    public News(String title, String content, String source) {
        this.title = title;
        this.content = content;
        this.source = source;
        this.publishedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
