package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class config {
    
    // --- DATABASE CONFIGURATION (UPDATED FOR SQLITE) ---
    private static final String URL = "jdbc:sqlite:4psDB.db"; 
    
    private static Connection conn = null;

    public static void connectDB() {
        try {
            if (conn == null || conn.isClosed()) {
                // Must load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC"); 
                conn = DriverManager.getConnection(URL);
                System.out.println("✅ Database connection established to 4psDB.db.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC Driver Missing: Make sure the 'sqlite-jdbc-xxx.jar' file is in your project build path.");
        } catch (SQLException e) {
            System.err.println("❌ Database Connection Error: " + e.getMessage());
        }
    }

    public static Object getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // --- Core method for INSERT, UPDATE, DELETE ---
    public void addRecord(String sql, Object... params) {
        // 'try-with-resources' ensures PreparedStatement is closed on exit, releasing the lock.
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Operation successful.
            } else if (sql.toLowerCase().startsWith("update") || sql.toLowerCase().startsWith("delete")) {
                 // Only show a warning for 0 rows on update/delete
                 System.out.println("⚠️ Record operation completed, but 0 rows affected. Check IDs or foreign keys.");
            }
        } catch (SQLException e) {
            // FIX: Enhanced Error Checking for common SQLite constraint failures
            if (e.getMessage().contains("SQLITE_BUSY")) {
                 System.out.println("❌ SQL ADD/UPDATE/DELETE Error: The database file is locked (SQLITE_BUSY). Ensure all ResultSets from prior SELECTs are closed.");
            } else if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("❌ Error: A unique entry (like an Email or combined primary key) already exists. Details: " + e.getMessage());
            } else if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
                 System.out.println("❌ Error: Foreign key constraint failed. Check if the ID of the parent record exists. Details: " + e.getMessage());
            } else {
                System.err.println("❌ SQL ADD/UPDATE/DELETE Error (" + sql + ") : " + e.getMessage());
            }
        }
    }

    public void updateRecord(String sql, Object... params) {
        addRecord(sql, params); 
        if (!sql.toLowerCase().startsWith("insert")) {
            System.out.println("✅ Record updated successfully!");
        }
    }
    
    public void deleteRecord(String sql, Object... params) {
        addRecord(sql, params); 
        if (!sql.toLowerCase().startsWith("insert")) {
            System.out.println("✅ Record deleted successfully!");
        }
    }

    // --- Core method for SELECT ---
    public ResultSet getRecords(String sql, Object... params) {
        PreparedStatement pstmt = null; // Declare outside try-catch to allow cleanup
        try {
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            
            // The calling code (AuthManager/RecordManager) MUST close the returned 
            // ResultSet using try-with-resources to release the database lock!
            return pstmt.executeQuery();
            
        } catch (SQLException e) {
            System.err.println("❌ SQL SELECT Error (" + sql + ") : " + e.getMessage());
            // If an error occurs, we must ensure the PreparedStatement is closed immediately.
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException closeE) { /* Ignore */ }
            }
            return null;
        }
    }

    public int getNextId(String tableName, String idColumn) {
        String sql = "SELECT MAX(" + idColumn + ") AS max_id FROM " + tableName;
        // Use try-with-resources for PreparedStatement and ResultSet to ensure immediate closing
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting next ID for " + tableName + ": " + e.getMessage());
        }
        return 1; 
    }

    public void displayResultSet(ResultSet rs) {
        if (rs == null) return;
        
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            // ... (Display logic) ...
            System.out.println("\n--- RESULTS ---");
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(String.format("%-25s", rsmd.getColumnLabel(i))); 
            }
            System.out.println();
            for (int i = 1; i <= columnCount * 25; i++) {
                System.out.print("-");
            }
            System.out.println();
            
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(String.format("%-25s", rs.getString(i)));
                }
                System.out.println();
            }
            
            if (!hasRows) {
                System.out.println("No records found.");
            }
            System.out.println("---------------");
        } catch (SQLException e) {
            System.err.println("❌ Error displaying ResultSet: " + e.getMessage());
        } finally {
            try {
                // Critical step: Ensure the ResultSet (and implicitly the PreparedStatement) is closed.
                if (rs != null) rs.close();
            } catch (SQLException e) {
                /* Ignore close error */
            }
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    public static void closeDB() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("✅ Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
}