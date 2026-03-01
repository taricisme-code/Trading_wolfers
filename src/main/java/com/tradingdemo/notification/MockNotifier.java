package com.tradingdemo.notification;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class MockNotifier implements Notifier {
    private final String logFile = "email_mock.log";

    @Override
    public void sendEmail(String to, String subject, String body) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            pw.println("--- MOCK EMAIL " + LocalDateTime.now() + " ---");
            pw.println("To: " + to);
            pw.println("Subject: " + subject);
            pw.println(body);
            pw.println();
        }
        System.out.println("[MockNotifier] Logged mock email to " + logFile);
    }
}
