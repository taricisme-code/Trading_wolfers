package com.tradingdemo.controller;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class ProfilController {
    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Button resetButton;

    @FXML
    private TextField phoneField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;
    private final AuthService authService = new AuthService();
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public void initialize() {
        backButton.setOnAction(e -> goBack());
        registerButton.setOnAction(event -> updateProfile());
        resetButton.setOnAction(event -> resetForm());
        loadUserData();
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
            Scene scene = new Scene(root);

            // Apply stylesheet
            try {
                String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Warning: Could not load stylesheet");
            }

            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("ERROR loading view: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load view: " + e.getMessage());
        }
    }

    @FXML
    private void updateProfile() {
        // Get input values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                phone.isEmpty() ) {
            AlertUtils.showWarning("Input Error", "All fields are required");
            return;
        }

        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            AlertUtils.showWarning("Input Error", "Please enter a valid email address");
            return;
        }

        try {
            // Attempt registration
            var user = authService.updateProfile(firstName, lastName, email, phone);
            if (user != null) {
                System.out.println("DEBUG: Registration successful, loading dashboard...");
                AlertUtils.showInfo("Success", "Account updated successfully! " + firstName);
                //loadDashboard();
            } else {
                System.out.println("DEBUG: Update failed");
                AlertUtils.showError("Update Failed", "Update Failed");
            }
        } catch (Exception e) {
            System.err.println("ERROR during Update: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Update Error", "An error occurred: " + e.getMessage());
        }
    }
    private void loadUserData() {
        var currentUser = AuthService.getCurrentUser();

        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
        }
    }
    @FXML
    private void resetForm() {

        var currentUser = AuthService.getCurrentUser();

        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());

            AlertUtils.showInfo("Reset", "Form restored successfully.");
        } else {
            AlertUtils.showError("Error", "No user is currently logged in.");
        }
    }
}
