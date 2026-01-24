package com.tradingdemo.service;

import com.tradingdemo.dao.UserDAO;
import com.tradingdemo.model.User;
import com.tradingdemo.util.PasswordUtils;

/**
 * AuthService - Business logic for authentication and user registration
 * Handles user login, registration, and password verification
 */
public class AuthService {

    private final UserDAO userDAO;
    private static User currentUser;

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
            user.setPasswordHash(hashedPassword);
            return userDAO.updateUser(user);
        }
        
        return false;
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
}
