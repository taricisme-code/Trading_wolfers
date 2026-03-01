package com.tradingdemo.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TOTPUtil {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public static boolean verifyCode(String base32Secret, int code) {
        try {
            return gAuth.authorize(base32Secret, code);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getOtpAuthURL(String accountName, String issuer, String secret) {
        try {
            String acct = URLEncoder.encode(accountName, "UTF-8");
            String iss = URLEncoder.encode(issuer, "UTF-8");
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", iss, acct, secret, iss);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
