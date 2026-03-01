package com.tradingdemo.notification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {
    private final Properties props = new Properties();

    public EmailConfig() {
        // Load defaults from classpath properties if present
        try (InputStream in = getClass().getResourceAsStream("/email.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {}
    }

    public String get(String key) {
        // Prefer environment variables, then properties file
        String val = System.getenv(key);
        if (val != null && !val.isEmpty()) return val;
        return props.getProperty(key);
    }

    public String getOrDefault(String key, String def) {
        String v = get(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
