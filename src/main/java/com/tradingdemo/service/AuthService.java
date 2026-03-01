package com.tradingdemo.service;

import com.tradingdemo.dao.UserDAO;
import com.tradingdemo.model.User;
import com.tradingdemo.util.PasswordUtils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * AuthService - Business logic for authentication and user registration
 * Handles user login, registration, and password verification
 */
public class AuthService {

    // SMTP configuration is now read from configuration (email.properties or env vars)
    // The helper method below no longer hard-codes credentials.  Alternatively the
    // code in initiatePasswordReset uses the NotifierFactory which already handles
    // configuration.

    private final UserDAO userDAO;
    private static User currentUser;
    private static IPInfoService.IPInfo sessionIPInfo;  // Current session IP info

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user based on email and password
     * @param email The user's email
     * @param password The user's plain text password
     * @return The authenticated User object or null if authentication fails
     */
    public User login(String email, String password) {
        try {
            User user = userDAO.getUserByEmail(email);
            
            if (user == null) {
                System.out.println("DEBUG: User not found with email: " + email);
                return null;
            }
            
            System.out.println("DEBUG: User found: " + user.getEmail() + " (ID: " + user.getId() + ")");
            System.out.println("DEBUG: Verifying password...");
            
            if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                currentUser = user;
                System.out.println("DEBUG: Login successful for: " + email);
                return user;
            } else {
                System.out.println("DEBUG: Password verification failed for: " + email);
                return null;
            }
        } catch (Exception e) {
            System.err.println("ERROR during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies a TOTP code for a user whose 2FA is enabled
     * @param user the user
     * @param code the 6-digit TOTP code
     * @return true if valid, false otherwise
     */
    public boolean verifyTwoFactor(User user, int code) {
        try {
            if (user == null || user.getTwoFactorSecret() == null) return false;
            return com.tradingdemo.util.TOTPUtil.verifyCode(user.getTwoFactorSecret(), code);
        } catch (Exception e) {
            System.err.println("ERROR verifying 2FA: " + e.getMessage());
            return false;
        }
    }

    /**
     * Enables two-factor authentication for a user by saving the provided secret
     * @param user the user to update
     * @param secret the Base32 secret to store
     * @return true if update succeeded
     */
    public boolean enableTwoFactorForUser(User user, String secret) {
        if (user == null || secret == null) return false;
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        return userDAO.updateUser(user);
    }

    /**
     * Disables two-factor authentication for a user (removes secret)
     * @param user the user to update
     * @return true if update succeeded
     */
    public boolean disableTwoFactorForUser(User user) {
        if (user == null) return false;
        user.setTwoFactorSecret(null);
        user.setTwoFactorEnabled(false);
        return userDAO.updateUser(user);
    }

    /**
     * Registers a new user
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email
     * @param phone User's phone number
     * @param password User's plain text password
     * @return The created User object or null if registration fails
     */
    public User register(String firstName, String lastName, String email, String phone, String password) {
        try {
            // Check if email already exists
            if (userDAO.emailExists(email)) {
                System.out.println("DEBUG: Email already exists: " + email);
                return null; // Email already registered
            }

            // Hash the password
            String hashedPassword = PasswordUtils.hashPassword(password);
            System.out.println("DEBUG: Password hashed successfully");

            // Create new user
            User newUser = new User(firstName, lastName, email, phone, hashedPassword);
            
            if (userDAO.createUser(newUser)) {
                currentUser = newUser;
                System.out.println("DEBUG: User registered successfully: " + email + " (ID: " + newUser.getId() + ")");
                return newUser;
            } else {
                System.out.println("DEBUG: Failed to create user in database");
                return null;
            }
        } catch (Exception e) {
            System.err.println("ERROR during registration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the currently logged in user
     * @return The current User object or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets the current session's IP information
     * @return IPInfo with location details or null if not available
     */
    public static IPInfoService.IPInfo getSessionIPInfo() {
        return sessionIPInfo;
    }

    /**
     * Sets the current session's IP information
     * @param ipInfo The IP information to store
     */
    public static void setSessionIPInfo(IPInfoService.IPInfo ipInfo) {
        sessionIPInfo = ipInfo;
    }

    /**
     * Sets the current user (for session management)
     * @param user The user to set as current
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Changes user password
     * @param userId The user ID
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password changed successfully, false otherwise
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDAO.getUserById(userId);
        
        if (user != null && PasswordUtils.verifyPassword(oldPassword, user.getPasswordHash())) {
            String hashedPassword = PasswordUtils.hashPassword(newPassword);
            return userDAO.updatePassword(userId, hashedPassword);
        }
        
        return false;
    }

    /**
     * Initiate a password reset by creating a reset token and sending it to the user's email.
     */
    public boolean initiatePasswordReset(String email) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user == null) return false;
            // generate 6-digit numeric code
            String code = String.format("%06d", (int) (Math.random() * 900000) + 100000);
            com.tradingdemo.dao.PasswordResetDAO prDao = new com.tradingdemo.dao.PasswordResetDAO();
            boolean created = prDao.createResetToken(user.getId(), code);
            if (!created) return false;
            // send email using the configured notifier instead of hard-coded helper
            try {
                var notifier = com.tradingdemo.notification.NotifierFactory.getNotifier();
                String subject = "Password reset code";
                String body = String.format("Your password reset code is: %s\nIt will expire in 15 minutes.", code);
                notifier.sendEmail(user.getEmail(), subject, body);
            } catch (Exception ex) {
                System.err.println("Failed to send reset email: " + ex.getMessage());
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error initiating password reset: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset a user's password using a previously generated reset code.
     */
    // Removed hard-coded helper; notifier pattern now handles email sending.  If
    // you need to construct messages manually you can still obtain configuration via
    // EmailConfig or call NotifierFactory as above.
    //
    // private void sendResetEmail(String to, String code) throws Exception { ... }
    // (method deleted)

    public boolean resetPasswordWithCode(String email, String code, String newPassword) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user == null) return false;
            com.tradingdemo.dao.PasswordResetDAO prDao = new com.tradingdemo.dao.PasswordResetDAO();
            boolean valid = prDao.verifyTokenForUser(user.getId(), code);
            if (!valid) return false;
            String hashed = PasswordUtils.hashPassword(newPassword);
            boolean ok = userDAO.updatePassword(user.getId(), hashed);
            if (ok) prDao.consumeToken(user.getId(), code);
            return ok;
        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates user profile information
     * @param user The updated user object
     * @return true if update successful, false otherwise
     */
    public boolean updateProfile(User user) {
        boolean success = userDAO.updateUser(user);
        if (success && currentUser != null && currentUser.getId() == user.getId()) {
            currentUser = user;
        }
        return success;
    }
    public User updateProfile(String firstName, String lastName, String email, String phone) {

        if (currentUser == null) {
            return null;
        }

        // Vérifier si l'email existe déjà pour un autre utilisateur
        User existingUser = userDAO.getUserByEmail(email);
        if (existingUser != null && existingUser.getId() != currentUser.getId()) {
            return null; // email déjà utilisé
        }

        // Mettre à jour les données
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        boolean updated = userDAO.updateUser(currentUser);

        return updated ? currentUser : null;
    }
}
