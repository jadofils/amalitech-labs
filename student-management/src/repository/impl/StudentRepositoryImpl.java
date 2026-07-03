package repository.impl;

import config.DatabaseConfig;
import exceptions.StudentNotFoundException;
import io.github.cdimascio.dotenv.Dotenv;
import model.HonorsStudent;
import model.RegularStudent;
import model.Student;
import model.enums.StudentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRepositoryImpl implements repository.StudentRepository{



    @Override
    public void addStudent(Student student) {
        // Query to add students with prepared statements
        String query = "INSERT INTO students(student_id, name, age, email, phone, status, student_type) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getName());
            ps.setInt(3, student.getAge());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getStatus().name());
            ps.setString(7, student.getStudentType());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Override
    public Student findStudentById(String studentId) {
        //query to find student by id
        String query="SELECT * FROM students WHERE student_id=?";
        try
        {
          Connection conn=DatabaseConfig.getConnection();
          PreparedStatement ps=conn.prepareStatement(query);
              ps.setString(1,studentId);
            ResultSet rs=ps.executeQuery();
            //check if the students exists
            if(rs.next()){
                return mapRowToStudent(rs);
            }
            else{
                throw new StudentNotFoundException("Student with ID " + studentId +  "not found");
            }

        }catch (SQLException e){
            e.printStackTrace();
        System.out.println("Error fetching student: " + e.getMessage());}
        return null;
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> students=new ArrayList<>();
        //query to find all students
        String query="SELECT * FROM students";
        try
        {
           Connection conn=DatabaseConfig.getConnection();
           Statement stmt =conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                students.add(mapRowToStudent(rs));
            }
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error fetching students: " + e.getMessage());
        }
        return students;

    }

    @Override
    public void updateStudent(Student student) {
        String sql = "UPDATE students SET name=?, age=?, email=?, phone=?, status=?, student_type=? WHERE student_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setInt(2, student.getAge());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getPhone());
            stmt.setString(5, student.getStatus().name());
            stmt.setString(6, student.getStudentType());
            stmt.setString(7, student.getStudentId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteStudent(String studentId) {
        String sql = "DELETE FROM students WHERE student_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to map DB row to Student object
    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        String type = rs.getString("student_type");
        String name = rs.getString("name");
        int age = rs.getInt("age");
        String email = rs.getString("email");
        String phone = rs.getString("phone");

        Student student;
        if ("REGULAR".equalsIgnoreCase(type)) {
            student = new RegularStudent(name, age, email, phone);
        } else {
            student = new HonorsStudent(name, age, email, phone);
        }

        // Set ID and status manually (since constructor generates new ID)
        student.setStudentId(rs.getString("student_id"));
        student.setStatus(StudentStatus.valueOf(rs.getString("status")));

        return student;
    }
}
