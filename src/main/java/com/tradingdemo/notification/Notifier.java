package com.tradingdemo.notification;

public interface Notifier {
    void sendEmail(String to, String subject, String body) throws Exception;
}
