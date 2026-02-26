package com.tradingdemo.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.tradingdemo.model.News;
import com.tradingdemo.service.AggregatedNewsService;
import com.tradingdemo.service.NewsService;
import com.tradingdemo.util.AlertUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * NewsController - Displays cryptocurrency news and market updates
 * Integrated with CryptoCompare API for real-time news feeds
 */
public class NewsController {

    @FXML private ListView<String> newsListView;
    @FXML private TextArea newsDetailArea;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private final NewsService newsService = new NewsService();
    private final AggregatedNewsService aggregatedNewsService = new AggregatedNewsService();
    private List<News> newsList;
    private Timeline autoRefreshTimeline;
    private boolean useRealApi = true;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    public void initialize() {
        // Set custom cell factory for black text
        newsListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #111827; -fx-font-size: 13px;");
                    }
                }
            };
            return cell;
        });
        
        refreshButton.setOnAction(e -> refreshNews());
        backButton.setOnAction(e -> goBack());
        
        newsListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                displayNewsDetails();
            }
        });
        
        // Single click preview
        newsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayNewsDetails();
            }
        });
        
        // Initial load
        refreshNews();
        
        // Start auto-refresh every 2 minutes (120 seconds)
        startAutoRefresh();
    }

    /**
     * Starts automatic news refresh every 2 minutes
     */
    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(120), e -> {
            System.out.println("Auto-refreshing news...");
            refreshNews();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * Stops auto-refresh timeline
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }

    private void refreshNews() {
        newsListView.getItems().clear();
        newsDetailArea.setText("Loading news from multiple sources...");
        updateStatus("Fetching latest news from CryptoCompare, CryptoPanic, CoinDesk, Cointelegraph...", false);
        
        // Try to fetch from real APIs first
        new Thread(() -> {
            try {
                System.out.println("Starting news fetch from aggregated sources...");
                List<News> apiNews = aggregatedNewsService.getAggregatedNews(100); // Fetch 100 from each source
                
                if (apiNews != null && !apiNews.isEmpty()) {
                    useRealApi = true;
                    newsList = apiNews;
                    
                    Platform.runLater(() -> {
                        displayNewsList();
                        updateStatus("✓ Live news: " + apiNews.size() + " articles from 4 sources", true);
                    });
                } else {
                    // Fallback to demo news
                    loadDemoNews();
                }
            } catch (Exception e) {
                System.err.println("Error loading real news: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> loadDemoNews());
            }
        }).start();
    }

    /**
     * Loads demo news as fallback
     */
    private void loadDemoNews() {
        useRealApi = false;
        newsService.initializeDemoNews();
        newsList = newsService.getAllNews();
        displayNewsList();
        updateStatus("⚠ Using demo news (API unavailable)", false);
    }

    /**
     * Displays news list in ListView
     */
    private void displayNewsList() {
        newsListView.getItems().clear();
        
        for (News news : newsList) {
            String formattedDate = news.getPublishedAt() != null ? 
                news.getPublishedAt().format(DATE_FORMATTER) : "Unknown Date";
            
            String displayText = String.format("[%s] %s - %s", 
                formattedDate, news.getSource(), news.getTitle());
            
            newsListView.getItems().add(displayText);
        }
        
        // Auto-select first item
        if (!newsList.isEmpty()) {
            newsListView.getSelectionModel().selectFirst();
            displayNewsDetails();
        }
    }

    private void displayNewsDetails() {
        int index = newsListView.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < newsList.size()) {
            News news = newsList.get(index);
            
            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════════════════════\n");
            details.append(news.getTitle().toUpperCase()).append("\n");
            details.append("═══════════════════════════════════════════════════════\n\n");
            
            details.append("📰 Source: ").append(news.getSource()).append("\n");
            
            if (news.getPublishedAt() != null) {
                details.append("📅 Published: ").append(news.getPublishedAt().format(DATE_FORMATTER)).append("\n");
            }
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                details.append("🏷️  Tags: ").append(news.getTags()).append("\n");
            }
            
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                details.append("🔗 URL: ").append(news.getUrl()).append("\n");
            }
            
            details.append("\n───────────────────────────────────────────────────────\n");
            details.append("ARTICLE CONTENT\n");
            details.append("───────────────────────────────────────────────────────\n\n");
            
            details.append(news.getContent());
            
            if (useRealApi) {
                details.append("\n\n───────────────────────────────────────────────────────\n");
                details.append("💡 Live news from multiple sources\n");
                details.append("Sources: CryptoCompare, CryptoPanic\n");
                details.append("Auto-refresh: Every 2 minutes\n");
            }
            
            newsDetailArea.setText(details.toString());
        }
    }

    /**
     * Updates status label
     */
    private void updateStatus(String message, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            if (success) {
                statusLabel.setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #f0883e; -fx-font-weight: bold;");
            }
        }
    }

    @FXML
    private void goBack() {
        stopAutoRefresh();
        loadView("/com/tradingdemo/view/dashboard.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load view");
        }
    }
}

