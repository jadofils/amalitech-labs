package config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static  String url;
    private static String user;
    private static String password;
static {
    //load the env variables from .env
    Dotenv dotenv= Dotenv.load();
    url=dotenv.get("DB_URL");
    user=dotenv.get("DB_USER");
    password=dotenv.get("DB_PASSWORD");

//LETS INITIALIZE TABLES WHEN CLASS IS LOADED
    initializeTable();
}


//get database connection here
// Get database connection
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, user, password);
}

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
//create ables if they are not exists
    private static void initializeTable() {
    try(Connection connection = getConnection(); Statement stmt = connection.createStatement()){
        String createStudentTable = """
        CREATE TABLE IF NOT EXISTS students (
            student_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            age INT NOT NULL,
            email VARCHAR(255) NOT NULL,
            phone VARCHAR(255) NOT NULL,
            status VARCHAR(255) NOT NULL,
            student_type VARCHAR(255) NOT NULL
        );
    """;

        String createGradesTable = """
        CREATE TABLE IF NOT EXISTS grades (
            id SERIAL PRIMARY KEY,
            student_id VARCHAR(255) REFERENCES students(student_id) ON DELETE CASCADE,
            grade NUMERIC(5,2) NOT NULL
        );
    """;

       stmt.executeUpdate(createStudentTable);
       stmt.executeUpdate(createGradesTable);
        System.out.println("Tables initialized successfully.");
    }
    catch (SQLException e) {
        System.err.println("Error initializing tables: " + e.getMessage());
    }
    }
}
