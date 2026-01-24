package com.tradingdemo.controller;

import java.io.IOException;
import java.util.List;

import com.tradingdemo.model.News;
import com.tradingdemo.service.NewsService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * NewsController - Displays cryptocurrency news and market updates
 */
public class NewsController {

    @FXML private ListView<String> newsListView;
    @FXML private TextArea newsDetailArea;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final NewsService newsService = new NewsService();
    private List<News> newsList;

    @FXML
    public void initialize() {
        newsService.initializeDemoNews();
        refreshButton.setOnAction(e -> refreshNews());
        backButton.setOnAction(e -> goBack());
        
        newsListView.setOnMouseClicked(e -> displayNewsDetails());
        
        refreshNews();
    }

    private void refreshNews() {
        newsListView.getItems().clear();
        newsList = newsService.getAllNews();
        
        for (News news : newsList) {
            newsListView.getItems().add(String.format("[%s] %s", 
                news.getSource(), news.getTitle()));
        }
    }

    private void displayNewsDetails() {
        int index = newsListView.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < newsList.size()) {
            News news = newsList.get(index);
            newsDetailArea.setText(String.format(
                "Title: %s\n\n" +
                "Source: %s\n" +
                "Published: %s\n\n" +
                "Content:\n%s",
                news.getTitle(),
                news.getSource(),
                news.getPublishedAt(),
                news.getContent()
            ));
        }
    }

    @FXML
    private void goBack() {
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
