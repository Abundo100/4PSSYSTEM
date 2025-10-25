package main;

import config.config;
import java.sql.SQLException;
import java.util.Scanner;

public class main {
    
    // --- Application State ---
    private static boolean loggedIn = false;
    private static int loggedUserId = -1; // Stores the ID of the currently logged-in user
    private static String loggedUserName = ""; // Stores the name of the currently logged-in user
    private static int loginAttempts = 0;
    
    // --- Helper Classes ---
    private static final Scanner sc = new Scanner(System.in);
    private static final config db = new config();
    private static final AuthManager auth = new AuthManager(db, sc);
    private static final RecordManager recordManager = new RecordManager(db, sc);
    
    public static void main(String[] args) {
        
        // 1. Establish DB connection (must be successful to continue)
        config.connectDB();
        
        // 2. Main Application Loop
        while (true) {
            try {
                if (!loggedIn) {
                    showPreLoginMenu();
                } else {
                    showMainMenu();
                }
            } catch (SQLException e) {
                System.out.println("❌ An unexpected Database error occurred: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ A fatal error occurred: " + e.getMessage());
                break; // Exit the loop on major error
            }
        }
        
        // 3. Cleanup
        config.closeDB(); 
        sc.close(); 
    }
    
    private static void showPreLoginMenu() throws SQLException {
        System.out.println("\n=== 4Ps EDUCATIONAL MONITORING SYSTEM ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose option: ");

        int firstChoice = getIntInput(sc.nextLine());

        switch (firstChoice) {
            case 1:
                // Calls AuthManager to handle registration
                auth.handleRegistration();
                break;
            case 2:
                // Calls AuthManager to handle login and update state
                AuthManager.LoginResult result = auth.handleLogin(loginAttempts);
                if (result.isSuccess()) {
                    loggedIn = true;
                    loggedUserId = result.getUserId();
                    loggedUserName = result.getUserName();
                    loginAttempts = 0;
                } else {
                    loginAttempts = result.getLoginAttempts();
                    if (loginAttempts >= 3) {
                         System.out.println("🚫 Too many failed login attempts. Exiting program...");
                         System.exit(0); // Force exit
                    }
                }
                break;
            case 3:
                System.out.println("Exiting program...");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void showMainMenu() throws SQLException {
        System.out.println("\n=== MAIN MENU (Welcome, " + loggedUserName + "!) ===");
        System.out.println("1. Add Program");
        System.out.println("2. Record Participation");
        System.out.println("3. Record Attendance");
        System.out.println("4. View Records");
        System.out.println("5. Update Record");
        System.out.println("6. Delete Record");
        System.out.println("7. Logout");
        System.out.print("Choose option: ");

        int choice = getIntInput(sc.nextLine());

        switch (choice) {
            case 1: recordManager.addProgram(); break;
            case 2: recordManager.recordParticipation(); break;
            case 3: recordManager.recordAttendance(); break;
            case 4: recordManager.viewRecords(); break;
            case 5: recordManager.updateRecord(); break;
            case 6: recordManager.deleteRecord(); break;
            case 7:
                // Logout Logic
                loggedIn = false;
                loggedUserId = -1;
                loggedUserName = "";
                System.out.println("👋 Logged out successfully!");
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    // --- Local Helper for Menu Input (replaces old getIntInput for first/menu choice) ---
    private static int getIntInput(String choiceLine) {
        try {
            return Integer.parseInt(choiceLine);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number.");
            return -1; // Sentinel value
        }
    }
}