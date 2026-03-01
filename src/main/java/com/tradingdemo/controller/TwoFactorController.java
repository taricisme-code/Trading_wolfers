package com.tradingdemo.controller;

import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.QRCodeUtil;
import com.tradingdemo.util.TOTPUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

public class TwoFactorController {

    @FXML private ImageView qrImageView;
    @FXML private TextArea instructionsArea;
    @FXML private TextField codeField;
    @FXML private Button enableButton;
    @FXML private Button printButton;
    @FXML private Label statusLabel;

    private com.tradingdemo.model.User user;
    private String secret;
    private String otpAuth;
    private final AuthService authService = new AuthService();

    public void initialize() {
        enableButton.setOnAction(e -> handleEnable());
        printButton.setOnAction(e -> handlePrint());
    }

    public void initData(com.tradingdemo.model.User user, String secret, String otpAuth) {
        this.user = user;
        this.secret = secret;
        this.otpAuth = otpAuth;

        try {
            File qrFile = File.createTempFile("qrcode_", ".png");
            qrFile.deleteOnExit();
            QRCodeUtil.generateQRCodeImage(otpAuth, 300, 300, qrFile.getAbsolutePath());

            try (FileInputStream fis = new FileInputStream(qrFile)) {
                Image img = new Image(fis);
                Platform.runLater(() -> qrImageView.setImage(img));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Steps to complete setup:\n\n");
            sb.append("1) Open your authenticator app (Google Authenticator, Authy, etc.).\n");
            sb.append("2) Scan the QR code displayed above.\n");
            sb.append("3) If you cannot scan, manually add account and use this secret: ").append(secret).append("\n");
            sb.append("4) Enter the 6-digit code from the app into the field and click Enable.\n");
            sb.append("\nKeep this secret safe. If you lose access, contact support to recover your account.");

            instructionsArea.setText(sb.toString());
        } catch (Exception e) {
            statusLabel.setText("Error generating QR: " + e.getMessage());
        }
    }

    private void handleEnable() {
        if (secret == null || user == null) return;
        String codeStr = Optional.ofNullable(codeField.getText()).orElse("").trim();
        try {
            int code = Integer.parseInt(codeStr);
            boolean ok = TOTPUtil.verifyCode(secret, code);
            if (ok) {
                boolean updated = authService.enableTwoFactorForUser(user, secret);
                if (updated) {
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                    statusLabel.setText("2FA enabled successfully.");
                    // close modal
                    Stage s = (Stage) enableButton.getScene().getWindow();
                    s.close();
                } else {
                    statusLabel.setText("Failed to persist 2FA settings.");
                }
            } else {
                statusLabel.setText("Invalid code. Please try again.");
            }
        } catch (NumberFormatException nfe) {
            statusLabel.setText("Code must be numeric.");
        }
    }

    private void handlePrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            Stage st = (Stage) printButton.getScene().getWindow();
            boolean proceed = job.showPrintDialog(st);
            if (proceed) {
                boolean printed = job.printPage(instructionsArea);
                if (printed) job.endJob();
            }
        }
    }
}
