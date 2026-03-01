package com.tradingdemo.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * IPInfoService - Integrates with IPInfo.io API to retrieve IP-based location information
 */
public class IPInfoService {

    private static final String IPINFO_API_URL = "https://ipinfo.io/json";
    private static final int TIMEOUT = 5000; // 5 seconds

    /**
     * Retrieves IP information including location, city, country, etc.
     * @return IPInfo with IP details or null if request fails
     */
    public static IPInfo getIPInfo() {
        try {
            URL url = new URL(IPINFO_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", "Trading_wolfers/1.0");

            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
                IPInfo info = new IPInfo();
                info.ip = json.has("ip") ? json.get("ip").getAsString() : "N/A";
                info.country = json.has("country") ? json.get("country").getAsString() : "N/A";
                info.city = json.has("city") ? json.get("city").getAsString() : "N/A";
                info.region = json.has("region") ? json.get("region").getAsString() : "N/A";
                info.isp = json.has("org") ? json.get("org").getAsString() : "N/A";
                info.timezone = json.has("timezone") ? json.get("timezone").getAsString() : "N/A";

                System.out.println("IPInfo retrieved: " + info.ip + " from " + info.country + ", " + info.city);
                return info;
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve IP info: " + e.getMessage());
        }
        return null;
    }

    /**
     * Simple data class to hold IP information
     */
    public static class IPInfo {
        public String ip;
        public String country;
        public String city;
        public String region;
        public String isp;
        public String timezone;

        @Override
        public String toString() {
            return String.format("IP: %s | Country: %s | City: %s | ISP: %s", ip, country, city, isp);
        }
    }
}
