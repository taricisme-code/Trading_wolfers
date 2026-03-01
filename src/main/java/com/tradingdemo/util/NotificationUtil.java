package com.tradingdemo.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.application.Platform;

public class NotificationUtil {
    public static void showPopup(String title, String message) {
        Platform.runLater(() -> {
            Stage stage = new Stage(StageStyle.UNDECORATED);

            Label titleLbl = new Label(title);
            titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

            Label msg = new Label(message);
            msg.setWrapText(true);
            msg.setMaxWidth(360);
            msg.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(6, titleLbl, msg);
            container.setStyle("-fx-padding: 12; -fx-background-color: rgba(17,24,39,0.95); -fx-border-radius: 8; -fx-background-radius: 8;");

            StackPane root = new StackPane(container);
            StackPane.setAlignment(container, Pos.TOP_CENTER);

            Scene scene = new Scene(root);
            scene.setFill(null);

            stage.setScene(scene);
            stage.setAlwaysOnTop(true);

            root.setOnMouseClicked(ev -> stage.close());

            stage.show();

            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), ev -> stage.close()));
            t.play();
        });
    }
}
