package repository.grade.impl;

import config.DatabaseConfig;
import exceptions.grades.GradeException;
import model.grade.Grade;
import model.subject.Subject;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;
import model.enums.SubjectType;
import repository.grade.GradeRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeRepositoryImpl implements GradeRepository {

    @Override
    public void addGrade(Grade grade) {
        String sql = "INSERT INTO grades (grade_id, student_id, subject_code, grade, date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, grade.getGradeId());
            pstmt.setString(2, grade.getStudentId());
            pstmt.setString(3, grade.getSubject().getSubjectCode());
            pstmt.setDouble(4, grade.getGrade());
            pstmt.setDate(5, java.sql.Date.valueOf(grade.getDate()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GradeException("Error adding grade: " + e.getMessage());
        }
    }

    @Override
    public Grade findGradeById(String gradeId) {
        String sql = "SELECT g.grade_id, g.student_id, g.subject_code, g.grade, g.date, s.subject_name, s.subject_type " +
                "FROM grades g JOIN subjects s ON g.subject_code = s.subject_code WHERE g.grade_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gradeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Subject subject = rs.getString("subject_type").equalsIgnoreCase("CORE")
                        ? new CoreSubject(rs.getString("subject_name"), rs.getString("subject_code"))
                        : new ElectiveSubject(rs.getString("subject_name"), rs.getString("subject_code"));

                return Grade.reconstruct(
                        rs.getString("grade_id"),
                        rs.getString("student_id"),
                        subject,
                        rs.getDouble("grade"),
                        rs.getDate("date").toString()
                );
            }
        } catch (SQLException e) {
            throw new GradeException("Error finding grade: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Grade> findGradesByStudentId(String studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.grade_id, g.student_id, g.subject_code, g.grade, g.date, s.subject_name, s.subject_type " +
                "FROM grades g JOIN subjects s ON g.subject_code = s.subject_code WHERE g.student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Subject subject = rs.getString("subject_type").equalsIgnoreCase("CORE")
                        ? new CoreSubject(rs.getString("subject_name"), rs.getString("subject_code"))
                        : new ElectiveSubject(rs.getString("subject_name"), rs.getString("subject_code"));

                grades.add(Grade.reconstruct(
                        rs.getString("grade_id"),
                        rs.getString("student_id"),
                        subject,
                        rs.getDouble("grade"),
                        rs.getDate("date").toString()
                ));
            }
        } catch (SQLException e) {
            throw new GradeException("Error finding grades by student: " + e.getMessage());
        }
        return grades;
    }

    @Override
    public List<Grade> getAllGrades() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.grade_id, g.student_id, g.subject_code, g.grade, g.date, s.subject_name, s.subject_type " +
                "FROM grades g JOIN subjects s ON g.subject_code = s.subject_code";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Subject subject = rs.getString("subject_type").equalsIgnoreCase("CORE")
                        ? new CoreSubject(rs.getString("subject_name"), rs.getString("subject_code"))
                        : new ElectiveSubject(rs.getString("subject_name"), rs.getString("subject_code"));

                grades.add(Grade.reconstruct(
                        rs.getString("grade_id"),
                        rs.getString("student_id"),
                        subject,
                        rs.getDouble("grade"),
                        rs.getDate("date").toString()
                ));
            }
        } catch (SQLException e) {
            throw new GradeException("Error retrieving all grades: " + e.getMessage());
        }
        return grades;
    }

    @Override
    public void deleteGrade(String gradeId) {
        String sql = "DELETE FROM grades WHERE grade_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gradeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GradeException("Error deleting grade: " + e.getMessage());
        }
    }
}
