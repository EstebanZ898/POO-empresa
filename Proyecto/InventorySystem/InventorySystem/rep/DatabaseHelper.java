package rep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/inventory_system";
    private static final String USER = "root";   // Replace with your MySQL username
    private static final String PASSWORD = "Perritos$89"; // Replace with your MySQL password

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection to MySQL has been established.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        String productTable = "CREATE TABLE IF NOT EXISTS products (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "date DATE NOT NULL, " +  // Added date column
                "code VARCHAR(50), type VARCHAR(50), subvariant VARCHAR(50), " +
                "color VARCHAR(50), costPerUnit DOUBLE, costPerGram DOUBLE, totalCost DOUBLE);";

        String salesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "invoiceNumber VARCHAR(50), customerName VARCHAR(50), " +
                "description VARCHAR(255), gramsUsed DOUBLE, price DOUBLE, date DATE, type VARCHAR(50));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(productTable);
                stmt.execute(salesTable);
                System.out.println("Database tables initialized.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to initialize database: " + e.getMessage());
        }
    }
}
