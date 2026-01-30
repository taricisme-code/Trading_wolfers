package com.tradingdemo;

import java.io.IOException;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for the Cryptocurrency Trading Demo.
 * Initializes JavaFX and loads the login screen.
 */
public class MainApp extends Application {

    private static final String APP_TITLE = "Crypto Trading Demo";
    private static final String LOGIN_VIEW = "/com/tradingdemo/view/login.fxml";

    /**
     * Starts the JavaFX application
     * @param stage The primary stage
     */
    @Override
    public void start(Stage stage) {
        try {
            // Load the login FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(LOGIN_VIEW)
            );
            Parent root = loader.load();

            // Create and configure the scene
            Scene scene = new Scene(root, 1200, 700);
            
            // Apply CSS stylesheet if available
            String css = Objects.requireNonNull(
                getClass().getResource("/com/tradingdemo/view/styles.css")
            ).toExternalForm();
            scene.getStylesheets().add(css);

            // Configure stage
            stage.setTitle(APP_TITLE);
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setMaximized(true); // Start maximized
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method to launch the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
