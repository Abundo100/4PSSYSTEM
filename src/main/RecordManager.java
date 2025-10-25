package main;

import config.config;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class RecordManager {

    private final config db;
    private final Scanner sc;

    public RecordManager(config db, Scanner sc) {
        this.db = db;
        this.sc = sc;
    }

    private int getIntInput(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a whole number.");
            return -1;
        }
    }

    private double getDoubleInput(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a valid number (e.g., 1500.00).");
            return -1.0;
        }
    }

    // ----------------------------------------------------
    // --- 1. ADD PROGRAM ---
    // ----------------------------------------------------
    public void addProgram() throws SQLException {
        int progId = db.getNextId("program", "program_id");
        System.out.println("\n--- ADD PROGRAM ---");
        System.out.println("Generated Program ID: " + progId);

        System.out.print("Enter Program Name: ");
        String progName = sc.nextLine().trim();
        System.out.print("Enter Description: ");
        String desc = sc.nextLine().trim();

        String sqlProgram = "INSERT INTO program(program_id, program_name, description) VALUES (?,?,?)";
        db.addRecord(sqlProgram, progId, progName, desc);
        System.out.println("✅ Program added successfully!");
    }

    // ----------------------------------------------------
    // --- 2. RECORD PARTICIPATION ---
    // ----------------------------------------------------
    public void recordParticipation() throws SQLException {
        System.out.println("\n--- RECORD PARTICIPATION ---");
        int pUserId = getIntInput("Enter User ID: ");
        if (pUserId == -1) return;

        int pProgId = getIntInput("Enter Program ID: ");
        if (pProgId == -1) return;

        int meetings = getIntInput("Enter Meetings Attended: ");
        if (meetings == -1) return;

        double amount = getDoubleInput("Enter Amount Received: ");
        if (amount == -1.0) return;

        String checkSql = "SELECT user_id FROM participation WHERE user_id = ? AND program_id = ?";
        ResultSet rsCheck = db.getRecords(checkSql, pUserId, pProgId);
        if (rsCheck != null && rsCheck.next()) {
            System.out.println("❌ Participation record already exists. Use Update (Option 5).");
            return;
        }

        String sqlParticipation = "INSERT INTO participation(user_id, program_id, meetings_attended, amount_received) VALUES (?,?,?,?)";
        db.addRecord(sqlParticipation, pUserId, pProgId, meetings, amount);
        System.out.println("✅ Participation recorded!");
    }

    // ----------------------------------------------------
    // --- 3. RECORD ATTENDANCE ---
    // ----------------------------------------------------
    public void recordAttendance() throws SQLException {
        System.out.println("\n--- RECORD ATTENDANCE ---");
        int attId = db.getNextId("attendance", "attendance_id");
        System.out.println("Generated Attendance ID: " + attId);

        int aUserId = getIntInput("Enter User ID: ");
        if (aUserId == -1) return;

        int aProgId = getIntInput("Enter Program ID: ");
        if (aProgId == -1) return;

        System.out.print("Enter Meeting Date (YYYY-MM-DD): ");
        String date = sc.nextLine().trim();

        String sqlAttendance = "INSERT INTO attendance(attendance_id, user_id, program_id, meeting_date) VALUES (?,?,?,?)";
        db.addRecord(sqlAttendance, attId, aUserId, aProgId, date);
        System.out.println("✅ Attendance recorded!");
    }

    // ----------------------------------------------------
    // --- 4. VIEW RECORDS ---
    // ----------------------------------------------------
    public void viewRecords() throws SQLException {
        System.out.println("\n--- VIEW RECORDS ---");
        System.out.println("1. Users");
        System.out.println("2. Programs");
        System.out.println("3. Participation");
        System.out.println("4. Attendance");
        System.out.print("Choose: ");

        int viewChoice = getIntInput("");
        if (viewChoice == -1) return;

        String sqlView = null;

        switch (viewChoice) {
            case 1:
                sqlView = "SELECT user_id, name, gender, year_level, email FROM user";
                break;
            case 2:
                sqlView = "SELECT * FROM program";
                break;
            case 3:
                sqlView = "SELECT * FROM participation";
                break;
            case 4:
                sqlView = "SELECT * FROM attendance";
                break;
            default:
                System.out.println("Invalid option!");
                return;
        }

        if (sqlView != null) {
            ResultSet rsView = db.getRecords(sqlView);
            db.displayResultSet(rsView);
        }
    }

    // ----------------------------------------------------
    // --- 5. UPDATE RECORD ---
    // ----------------------------------------------------
    public void updateRecord() throws SQLException {
        System.out.println("\n--- UPDATE RECORD ---");
        System.out.println("1. Program");
        System.out.println("2. Participation");
        System.out.print("Choose: ");
        int updateChoice = getIntInput("");
        if (updateChoice == -1) return;

        switch (updateChoice) {
            case 1:
                int progId = getIntInput("Enter Program ID to update: ");
                String checkSql1 = "SELECT * FROM program WHERE program_id = ?";
                ResultSet rs1 = db.getRecords(checkSql1, progId);

                if (rs1 == null || !rs1.next()) {
                    System.out.println("❌ Program not found!");
                    return;
                }

                System.out.println("Current Program Name: " + rs1.getString("program_name"));
                System.out.println("Current Description: " + rs1.getString("description"));
                System.out.print("Enter new Program Name: ");
                String newName = sc.nextLine().trim();
                System.out.print("Enter new Description: ");
                String newDesc = sc.nextLine().trim();

                String sqlUpdate1 = "UPDATE program SET program_name = ?, description = ? WHERE program_id = ?";
                db.updateRecord(sqlUpdate1, newName, newDesc, progId);
                System.out.println("✅ Program updated successfully!");
                break;

            case 2:
                int userId = getIntInput("Enter User ID: ");
                int progId2 = getIntInput("Enter Program ID: ");
                String checkSql2 = "SELECT * FROM participation WHERE user_id = ? AND program_id = ?";
                ResultSet rs2 = db.getRecords(checkSql2, userId, progId2);

                if (rs2 == null || !rs2.next()) {
                    System.out.println("❌ Participation record not found!");
                    return;
                }

                System.out.println("Current Meetings Attended: " + rs2.getInt("meetings_attended"));
                System.out.println("Current Amount Received: " + rs2.getDouble("amount_received"));
                int newMeetings = getIntInput("Enter new Meetings Attended: ");
                double newAmount = getDoubleInput("Enter new Amount Received: ");

                String sqlUpdate2 = "UPDATE participation SET meetings_attended = ?, amount_received = ? WHERE user_id = ? AND program_id = ?";
                db.updateRecord(sqlUpdate2, newMeetings, newAmount, userId, progId2);
                System.out.println("✅ Participation updated successfully!");
                break;

            default:
                System.out.println("Invalid option!");
                break;
        }
    }

    // ----------------------------------------------------
    // --- 6. DELETE RECORD ---
    // ----------------------------------------------------
    public void deleteRecord() throws SQLException {
        System.out.println("\n--- DELETE RECORD ---");
        System.out.println("1. Program");
        System.out.println("2. Participation");
        System.out.println("3. Attendance");
        System.out.print("Choose: ");
        int delChoice = getIntInput("");
        if (delChoice == -1) return;

        switch (delChoice) {
            case 1:
                int progId = getIntInput("Enter Program ID to delete: ");
                String checkSql1 = "SELECT * FROM program WHERE program_id = ?";
                ResultSet rs1 = db.getRecords(checkSql1, progId);
                if (rs1 == null || !rs1.next()) {
                    System.out.println("❌ Program not found!");
                    return;
                }

                System.out.print("Are you sure you want to delete this Program? (y/n): ");
                if (!sc.nextLine().equalsIgnoreCase("y")) {
                    System.out.println("❌ Deletion cancelled.");
                    return;
                }

                String sqlDel1 = "DELETE FROM program WHERE program_id = ?";
                db.deleteRecord(sqlDel1, progId);
                System.out.println("🗑️ Program deleted successfully!");
                break;

            case 2:
                int userId = getIntInput("Enter User ID: ");
                int progId2 = getIntInput("Enter Program ID: ");
                String checkSql2 = "SELECT * FROM participation WHERE user_id = ? AND program_id = ?";
                ResultSet rs2 = db.getRecords(checkSql2, userId, progId2);
                if (rs2 == null || !rs2.next()) {
                    System.out.println("❌ Participation record not found!");
                    return;
                }

                System.out.print("Are you sure you want to delete this Participation? (y/n): ");
                if (!sc.nextLine().equalsIgnoreCase("y")) {
                    System.out.println("❌ Deletion cancelled.");
                    return;
                }

                String sqlDel2 = "DELETE FROM participation WHERE user_id = ? AND program_id = ?";
                db.deleteRecord(sqlDel2, userId, progId2);
                System.out.println("🗑️ Participation deleted successfully!");
                break;

            case 3:
                int attId = getIntInput("Enter Attendance ID to delete: ");
                String checkSql3 = "SELECT * FROM attendance WHERE attendance_id = ?";
                ResultSet rs3 = db.getRecords(checkSql3, attId);
                if (rs3 == null || !rs3.next()) {
                    System.out.println("❌ Attendance record not found!");
                    return;
                }

                System.out.print("Are you sure you want to delete this Attendance? (y/n): ");
                if (!sc.nextLine().equalsIgnoreCase("y")) {
                    System.out.println("❌ Deletion cancelled.");
                    return;
                }

                String sqlDel3 = "DELETE FROM attendance WHERE attendance_id = ?";
                db.deleteRecord(sqlDel3, attId);
                System.out.println("🗑️ Attendance deleted successfully!");
                break;

            default:
                System.out.println("Invalid option!");
                break;
        }
    }
}
