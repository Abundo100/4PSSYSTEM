package main;

import config.config;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthManager {

    private final config db;
    private final Scanner sc;

    public AuthManager(config db, Scanner sc) {
        this.db = db;
        this.sc = sc;
    }
    
    public static class LoginResult {
        private final boolean success;
        private final int userId;
        private final String userName;
        private final int loginAttempts;

        public LoginResult(boolean success, int userId, String userName, int loginAttempts) {
            this.success = success;
            this.userId = userId;
            this.userName = userName;
            this.loginAttempts = loginAttempts;
        }

        public boolean isSuccess() { return success; }
        public int getUserId() { return userId; }
        public String getUserName() { return userName; }
        public int getLoginAttempts() { return loginAttempts; }
    }


    public void handleRegistration() throws SQLException {
        System.out.println("\n--- NEW USER REGISTRATION ---");
        int userId = db.getNextId("user", "user_id"); 
        System.out.println("Generated User ID: " + userId);
        
        System.out.print("Enter Name: ");
        String uname = sc.nextLine().trim();
        System.out.print("Enter Gender: ");
        String gender = sc.nextLine().trim();
        System.out.print("Enter Year Level: ");
        String year = sc.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        String hashedPassword = hashPassword(password);

        String checkEmailSQL = "SELECT * FROM user WHERE email = ?";
        
        try (ResultSet rsCheck = db.getRecords(checkEmailSQL, email)) {
            if (rsCheck != null && rsCheck.next()) {
                System.out.println("❌ Email already registered. Try logging in.");
                return;
            }
        }
        
        String sqlUser = "INSERT INTO user(user_id, name, gender, year_level, email, password) VALUES (?,?,?,?,?,?)";
        
        try {
            db.addRecord(sqlUser, userId, uname, gender, year, email, hashedPassword);
            System.out.println("✅ Registration successful!");
        } catch (Exception e) {
            System.out.println("❌ Registration failed due to a database error: " + e.getMessage());
        }
    }

    public LoginResult handleLogin(int currentAttempts) throws SQLException {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Enter Email: ");
        String loginEmail = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String loginPass = sc.nextLine().trim();
        String hashedLogin = hashPassword(loginPass);

        String sqlLogin = "SELECT user_id, name FROM user WHERE email = ? AND password = ?";
        
        try (ResultSet rsLogin = db.getRecords(sqlLogin, loginEmail, hashedLogin)) {
            if (rsLogin != null && rsLogin.next()) {
                int userId = rsLogin.getInt("user_id");
                String userName = rsLogin.getString("name");
                System.out.println("✅ Login successful! Welcome, " + userName + " (ID: " + userId + ")!");
                return new LoginResult(true, userId, userName, 0);
            } else {
                int newAttempts = currentAttempts + 1;
                int remaining = 3 - newAttempts;
                System.out.println("❌ Invalid email or password. (" + remaining + " attempts left)");
                return new LoginResult(false, -1, "", newAttempts);
            }
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);    
        }
    }
}