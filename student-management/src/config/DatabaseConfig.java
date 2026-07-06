//package config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//public class DatabaseConfig {
//    private static String url;
//    private static String user;
//    private static String password;
//
//    static {
//        // Load environment variables from .env
//        Dotenv dotenv = Dotenv.load();
//        url = dotenv.get("DB_URL");
//        user = dotenv.get("DB_USER");
//        password = dotenv.get("DB_PASSWORD");
//
//        // Initialize tables when class is loaded
//        initializeTables();
//    }
//
//    // Get database connection
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(url, user, password);
//    }
//
//    public static boolean testConnection() {
//        try (Connection connection = getConnection()) {
//            return connection != null && !connection.isClosed();
//        } catch (SQLException e) {
//            System.out.println("Database connection failed: " + e.getMessage());
//            return false;
//        }
//    }
//
//    // Create tables if they do not exist
//    private static void initializeTables() {
//        try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
//
//            // Students table
//            String createStudentTable = """
//                CREATE TABLE IF NOT EXISTS students (
//                    student_id VARCHAR(255) PRIMARY KEY,
//                    name VARCHAR(255) NOT NULL,
//                    age INT NOT NULL,
//                    email VARCHAR(255) UNIQUE NOT NULL,
//                    phone VARCHAR(255),
//                    status VARCHAR(50) NOT NULL,
//                    student_type VARCHAR(50) NOT NULL
//                );
//            """;
//
//            // Subjects table
//            String createSubjectTable = """
//                CREATE TABLE IF NOT EXISTS subjects (
//                    subject_code VARCHAR(50) PRIMARY KEY,
//                    subject_name VARCHAR(100) NOT NULL,
//                    subject_type VARCHAR(20) NOT NULL
//                );
//            """;
//
//            // Grades table
//            String createGradesTable = """
//                CREATE TABLE IF NOT EXISTS grades (
//                    grade_id VARCHAR(50) PRIMARY KEY,
//                    student_id VARCHAR(255) NOT NULL,
//                    subject_code VARCHAR(50) NOT NULL,
//                    grade NUMERIC(5,2) NOT NULL,
//                    date DATE NOT NULL,
//                    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
//                    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE
//                );
//            """;
//
//            stmt.executeUpdate(createStudentTable);
//            stmt.executeUpdate(createSubjectTable);
//
//            migrateStaleGradesTable(stmt);
//            stmt.executeUpdate(createGradesTable);
//
//            seedSubjects(stmt);
//
//            System.out.println(" Tables initialized successfully.");
//        } catch (SQLException e) {
//            System.err.println("Error initializing tables: " + e.getMessage());
//        }
//    }
//
//    // Older versions of this app created a "grades" table without a subject_code column.
//    // CREATE TABLE IF NOT EXISTS leaves such a stale table untouched, so drop it here if it
//    // doesn't match the current schema and let the CREATE TABLE below rebuild it correctly.
//    private static void migrateStaleGradesTable(Statement stmt) throws SQLException {
//        String checkColumnSql = """
//            SELECT 1 FROM information_schema.columns
//            WHERE table_schema = current_schema() AND table_name = 'grades' AND column_name = 'subject_code'
//        """;
//        try (ResultSet rs = stmt.executeQuery(checkColumnSql)) {
//            if (!rs.next()) {
//                stmt.executeUpdate("DROP TABLE IF EXISTS grades");
//            }
//        }
//    }
//
//    // Seeds the fixed set of Core/Elective subjects the app offers when recording grades
//    private static void seedSubjects(Statement stmt) throws SQLException {
//        String seedSql = """
//            INSERT INTO subjects (subject_code, subject_name, subject_type) VALUES
//                ('MATH01', 'Mathematics', 'CORE'),
//                ('ENGL01', 'English', 'CORE'),
//                ('SCIE01', 'Science', 'CORE'),
//                ('MUSC01', 'Music', 'ELECTIVE'),
//                ('ART01', 'Art', 'ELECTIVE'),
//                ('PHED01', 'Physical Education', 'ELECTIVE')
//            ON CONFLICT (subject_code) DO NOTHING;
//        """;
//        stmt.executeUpdate(seedSql);
//    }
//}
