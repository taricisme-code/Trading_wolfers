package com.tradingdemo.notification;

public class NotifierFactory {
    public static Notifier getNotifier() {
        Notifier n = null;
        try {
            n = new SMTPNotifier();
            System.out.println("NotifierFactory: Using SMTPNotifier");
            return n;
        } catch (Exception ex) {
            System.err.println("NotifierFactory: SMTP not available: " + ex.getMessage());
        }

        n = new MockNotifier();
        System.out.println("NotifierFactory: Using MockNotifier (logged to file)");
        return n;
    }
}
